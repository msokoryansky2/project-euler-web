package controllers

import javax.inject._

import play.api.mvc._

@Singleton
class WebsocketTestController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {
  def index() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.websocket_test("ws://echo.websocket.org/"))
  }
}
