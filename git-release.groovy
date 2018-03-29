/*
This Groovy script is executed during a Jenkins Release build & will accomplish two things:

(1) Create a tag in GitHub with name v{RELEASEVERSION}
(2) Create an official GitHub release which can be viewed here: https://github.com/TheRealGD/therealgd/releases
*/

class GitHubRelease {
    String tag_name
    String target_commitish
    String name
    String body
    boolean draft
    boolean prerelease

    GitHubRelease(tag_name, target_commitish, name, body, draft, prerelease) {
      this.tag_name = tag_name
      this.target_commitish = target_commitish
      this.name = name
      this.body = body
      this.draft = draft
      this.prerelease = prerelease
    }
}

static void main(String[] args) {
  def githubCredentials = args[0]
  def remote_with_branch = args[1]
  def releaseVersion = 'v' + args[2]
  def releaseNotes = args[3]

  //
  // Parse Git branch from Jenkins env var GIT_BRANCH (which contains remote like, remote/branch)
  //
  def regex = /origin\/(.*)/
  def matcher = (remote_with_branch =~ regex)
  def branch = matcher[0][1]

  //
  // Create a JSON string to pass to curl command
  //
  def release = new GitHubRelease(releaseVersion, branch, releaseVersion, releaseNotes, false, false)
  def json = new groovy.json.JsonBuilder(release)
  def jsonstring = json.toString()

  //
  // POST JSON data with Curl resulting in creation of GitHub release
  //
  def curlString = String.format("curl --request POST --url https://%s@api.github.com/repos/therealgd/therealgd/releases --data '%s' -o /dev/stdout", githubCredentials, jsonstring)
  curl = ['bash', '-c', curlString]
  proc = curl.execute()
  body = new StringBuffer()
  headers = new StringBuffer()
  proc.consumeProcessOutput(body, headers)
  proc.waitFor()
}