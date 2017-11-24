package actors

import javax.inject.Inject

import msg.{MsgSolution, MsgSolveWorker}
import akka.actor.Actor
import akka.dispatch.MessageDispatcher
import play.api.Configuration
import services.{AwsLambdaEulerProblemService, EulerProblemService}


class EulerProblemWorker @Inject() (configuration: Configuration) extends Actor {
  // EulerProblemWorker does the heavy lifting with solving PE problems, many of which will take a dozen seconds or more.
  // Because of that we do not want to use the default dispatcher but instead use a separate thread pool dispatcher.
  // This should improve responsiveness somewhat.
  //
  // To use the default dispatcher we would have done: import context.dispatcher
  implicit val blockingDispatcher: MessageDispatcher = context.system.dispatchers.lookup("euler-blocking-context")

  // List of problems to be solved on AWS Lambda as opposed to locally
  val awsLambdaProblems: Seq[String] = configuration.getOptional[Seq[String]]("project_euler.aws_lambda").getOrElse(Seq[String]())
  def isViaLambda(problemNumber: Int): Boolean = awsLambdaProblems.contains(problemNumber.toString)

  // AWS credentials to be used in Lambda invokation
  val awsCredentialsAccess: String = configuration.getOptional[String]("aws_access_key_id").getOrElse("")
  val awsCredentialsSecret: String = configuration.getOptional[String]("aws_secret_access_key").getOrElse("")

  val logger = play.api.Logger(getClass)
  logger.info(s"Creating Euler problem worker $self")

  def receive: Receive = {
    case MsgSolveWorker(problemNumber) =>
      logger.info(s"Worker $self received request for problem # $problemNumber")
      sender ! MsgSolution(problemNumber, answer(problemNumber), isViaLambda(problemNumber))
  }

  /**
    * Obtain a String answer to specified Project Euler problem, whether by solving on AWS Lambda or a local solution
    */
  def answer(problemNumber: Int): String = {
    if (isViaLambda(problemNumber))
      AwsLambdaEulerProblemService.answer(problemNumber, awsCredentialsAccess, awsCredentialsSecret)
    else
      EulerProblemService.answer(problemNumber)
  }

}
