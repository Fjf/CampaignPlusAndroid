We are building an android app, you have access to the android studio mcp server.
The device you are debugging is typically the only one connected in `adb devices`, and the applicationid is `com.example.campaignplus`.
Multiple agents are working in this same directory at the same time, so sometimes a gradle sync might fail because of changes made by another, please don't attempt to fix issues that are out of scope for your current task.

After implementing a feature/fixing a bug, run a build to ensure there are no issues from your changed code.
