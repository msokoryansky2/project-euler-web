package services

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.regions.Regions
import com.amazonaws.services.lambda.AWSLambdaClientBuilder
import com.amazonaws.services.lambda.model.InvokeRequest


object AwsLambdaEulerProblemService {
  def answer(num: Int, awsCredentialsAccess: String, awsCredentialsSecret: String): String = {

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
      else
        invokeResult.getPayload.asCharBuffer.toString
    } catch {
      case e: Exception => "AWS Lambda Error :("
    }
  }
}
