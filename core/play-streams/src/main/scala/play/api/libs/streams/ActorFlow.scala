/*
 * Copyright (C) 2009-2019 Lightbend Inc. <https://www.lightbend.com>
 */

package play.api.libs.streams

import akka.actor._
import akka.stream.Materializer
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Keep
import akka.stream.scaladsl.Source
import akka.stream.scaladsl.Flow

/**
 * Provides a flow that is handled by an actor.
 *
 * See https://github.com/akka/akka/issues/16985.
 */
object ActorFlow {
  /**
   * Create a flow that is handled by an actor.
   *
   * Messages can be sent downstream by sending them to the actor passed into the props function.  This actor meets
   * the contract of the actor returned by [[https://doc.akka.io/api/akka/2.6/akka/stream/scaladsl/Source$.html#actorRef[T](bufferSize:Int,overflowStrategy:akka.stream.OverflowStrategy):akka.stream.scaladsl.Source[T,akka.actor.ActorRef]] akka.stream.scaladsl.Source.actorRef]].
   *
   * The props function should return the props for an actor to handle the flow. This actor will be created using the
   * passed in [[https://doc.akka.io/api/akka/2.6/akka/actor/ActorRefFactory.html akka.actor.ActorRefFactory]]. Each message received will be sent to the actor - there is no back pressure,
   * if the actor is unable to process the messages, they will queue up in the actors mailbox. The upstream can be
   * cancelled by the actor terminating itself.
   *
   * @param props A function that creates the props for actor to handle the flow.
   * @param bufferSize The maximum number of elements to buffer.
   * @param overflowStrategy The strategy for how to handle a buffer overflow.
   */
  def actorRef[In, Out](
      props: ActorRef => Props,
      bufferSize: Int = 16,
      overflowStrategy: OverflowStrategy = OverflowStrategy.dropNew
  )(implicit factory: ActorRefFactory, mat: Materializer): Flow[In, Out, _] = {
    val (outActor, publisher) = Source
      .actorRef[Out](bufferSize, overflowStrategy)
      .toMat(Sink.asPublisher(false))(Keep.both)
      .run()

    Flow.fromSinkAndSource(
      Sink.actorRef(
        factory.actorOf(Props(new Actor {
          val flowActor = context.watch(context.actorOf(props(outActor), "flowActor"))

          def receive = {
            case Status.Success(_) | Status.Failure(_) => flowActor ! PoisonPill
            case Terminated(_)                         => context.stop(self)
            case other                                 => flowActor ! other
          }

          override def supervisorStrategy = OneForOneStrategy() {
            case _ => SupervisorStrategy.Stop
          }
        })),
        Status.Success(())
      ),
      Source.fromPublisher(publisher)
    )
  }
}
