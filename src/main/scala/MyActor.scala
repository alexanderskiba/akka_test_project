import akka.actor.Actor

class MyActor extends Actor {
  //метод receive возвращает исходное поведение актора.
  //Это частично вычислимая фунекция, используемая  Akka для обработки сообщений, отправляемых актору
  override def receive: Receive = ???

}
