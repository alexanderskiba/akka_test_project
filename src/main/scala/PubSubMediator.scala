import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import javax.xml.stream.EventFilter

import scala.concurrent.duration.Duration

object PubSubMediator {
  case class Publish(topic: String, message: Any)
  case class Published(publish: Publish)

  case class Subscribe(topic: String, subscriber: ActorRef)
  case class Subscribed(subscribe: Subscribe)
  case class AlreadySubscribed(subscribe: Subscribe)

  case class Unsubscribe(topic: String, subscriber: ActorRef)
  case class Unsubscribed(unsubscribe: Unsubscribe)
  case class NotSubscribed(unsubscribe: Unsubscribe)

  case class GetSubscribers(topic: String)

  final val Name = "pub-sub-mediator"

  def props: Props = Props(new PubSubMediator)
}
//базовй актор для нашей библиотеки PubSub
class PubSubMediator extends Actor{
//  override def receive: Receive = Actor.emptyBehavior
  //emptyBehavior - функция, для которой не определено значение
//  val system = ActorSystem("pub-sub-mediator-spec-system")
  // вызовем фабричный метод, чтобы создать систему акторов, ибо невозможно просто создавать акторы
  // при непостредственном вызове конструктора система выбросит исключение
//  system.actorOf(Props(new PubSubMediator), "pub-sub-mediator")
  //обмен информацией с актором возможен только через асинхронные сообщения
  //Props - конфигурационный объект для актора, он принимает конструктор как параметр, передаваемый по имени
  // и может содержать другую важную информацию - например, о маршрутизации или развертывании
  import PubSubMediator._

  private var subscribers = Map.empty[String, Set[ActorRef]].withDefaultValue(Set.empty)

  override def receive = {
    case publish @ Publish(topic, message) =>
      subscribers(topic).foreach(_ ! message)
      sender() ! Published(publish)

    case subscribe @ Subscribe(topic, subscriber) if subscribers(topic).contains(subscriber) =>
      sender() ! AlreadySubscribed(subscribe)

    case subscribe @ Subscribe(topic, subscriber) =>
      subscribers += topic -> (subscribers(topic) + subscriber)
      sender() ! Subscribed(subscribe)

    case unsubscribe @ Unsubscribe(topic, subscriber) if !subscribers(topic).contains(subscriber) =>
      sender() ! NotSubscribed(unsubscribe)

    case unsubscribe @ Unsubscribe(topic, subscriber) =>
      subscribers += topic -> (subscribers(topic) - subscriber)
      sender() ! Unsubscribed(unsubscribe)

    case GetSubscribers(topic) =>
      sender() ! subscribers(topic)
  }
}

class PubSubMediatorSpec extends WordSpec with Matchers with BeforeAndAfterAll {
  implicit val system = ActorSystem("pub-sub-mediator-spec-system")

  "A PubSubMediator" should {
    "be suited for getting started" in {
      EventFilter.debug(occurenes = 1, pattern = s"started.*${classOf[PubSubMediator].getName}").intercept {
        system.actorOf(PubSubMediator.props)
      }
    }
  }

  override protected def afterAll() = {
    Await.ready(system.terminate(),Duration.inf)
    super.afterAll()
  }


}


