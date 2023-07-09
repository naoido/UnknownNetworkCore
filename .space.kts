/**
* JetBrains Space Automation
* This Kotlin script file lets you automate build activities
* For more info, see https://www.jetbrains.com/help/space/automation.html
*/

job("Build") {
  container(displayName = "Run gradle build", image = "openjdk:17") {
    env["CI"] = "true" // for paperweight, do not decompile re-mapped minecraft jar, to increase performance (execution time --;) and reduce memory usage
    kotlinScript { api ->
      api.gradlew("clean downloadWaterfall reCompressWaterfall compileJava jar shadowJar reobfJar")
  	}
  }
}