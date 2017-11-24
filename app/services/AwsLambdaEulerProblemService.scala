package services

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.regions.Regions
import com.amazonaws.services.lambda.AWSLambdaClientBuilder
import com.amazonaws.services.lambda.model.InvokeRequest


object AwsLambdaEulerProblemService {
  def answer(num: Int, awsCredentialsAccess: String, awsCredentialsSecret: String): String = {
    val logger = play.api.Logger(getClass)

    val invokeRequest = new InvokeRequest()
      .withFunctionName("projectEulerSolution")
      .withPayload(num.toString)

    val awsCreds = new BasicAWSCredentials(awsCredentialsAccess, awsCredentialsSecret)

    val awsLambda = AWSLambdaClientBuilder.standard.withRegion(Regions.US_EAST_1)
      .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
      .build

    try {
      val invokeResult = awsLambda.invoke(invokeRequest)
      if (invokeResult.getStatusCode < 200 || invokeResult.getStatusCode >= 300)
        "AWS Lambda Status " + invokeResult.getStatusCode + " :("
      else {
        // This is ugly but good enough for demo purposes. Payload in case of a successful execution
        // is an entire HTTP response with a bunch of headers as well as the answer we want.
        // Every line is terminated with \r\n and last such line is our desired answer

        val rawResponse = invokeResult.getPayload.asCharBuffer.toString

        logger.info(s"AWS Lambda raw response for problem $num is: \n" + rawResponse)

        val answer = rawResponse.split("\r\n").filter(!_.isEmpty).last

        logger.info(s"AWS Lambda answer for problem $num is: $answer")

        answer

      }
    } catch {
      case e: Exception => "AWS Lambda Error :("
    }
  }
}
