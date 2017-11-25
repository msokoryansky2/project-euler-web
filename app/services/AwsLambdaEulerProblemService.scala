package services

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.regions.Regions
import com.amazonaws.services.lambda.AWSLambdaClientBuilder
import com.amazonaws.services.lambda.model.InvokeRequest
import play.api.libs.json.{JsDefined, JsLookupResult, JsUndefined, Json}

object AwsLambdaEulerProblemService {
  def answer(num: Int, awsCredentialsAccess: String, awsCredentialsSecret: String): String = {
    val logger = play.api.Logger(getClass)

    val invokeRequest = new InvokeRequest()
      .withFunctionName("projectEulerSolutionJson")
      .withPayload(num.toString)

    val awsCreds = new BasicAWSCredentials(awsCredentialsAccess, awsCredentialsSecret)

    val awsLambda = AWSLambdaClientBuilder.standard.withRegion(Regions.US_EAST_1)
      .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
      .build

    try {
      val invokeResult = awsLambda.invoke(invokeRequest)
      if (invokeResult.getStatusCode < 200 || invokeResult.getStatusCode >= 300)
        "AWS λ Status " + invokeResult.getStatusCode + " :("
      else {
        val jsonString  = new String(invokeResult.getPayload.array, "UTF-8")
        Json.parse(jsonString) \ "answer" match {
          case JsDefined(v) => v.toString.replace("\"", "").replace("'", "")
          case undefined: JsUndefined => "AWS λ Json Error :("
        }
      }
    } catch {
      case e: Exception => "AWS λ Error :("
    }
  }
}
