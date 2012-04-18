# Kii File Storage Sample App#

A file backup that uses **Kii Cloud File Storage**. It demonstrates:

- Register and login users
- Backup and download file
- Move working file to trash and restore from trash 
- Viewer for working files and trashed files
- Viewer for checkiing the uploading and downloading progress

![Screen shots](https://github.com/kii-dev-jenkins/KiiFileStorageSampleApp/raw/master/doc/screen_shots.png)

- Show interoperability with Sync SDK, PC and web

![interoperability with sync SDK, PC and web](https://github.com/kii-dev-jenkins/KiiFileStorageSampleApp/raw/master/doc/Interoperability.png)


#Getting Started#

##Requirements##
- Support Android 2.2 and above


##Installation and Setup##

- Download the [sample app](https://github.com/kii-dev-jenkins/KiiFileStorageSampleApp/zipball/master).

- If you are developing in Eclipse with the ADT Plugin, create a project for the "KiiBoard" sample app by starting a new Android Project, selecting "Create project from existing source".

- Update the sample app with your own application ID and application key at [Constants class](https://github.com/kii-dev-jenkins/KiiFileStorageSampleApp/blob/master/src/com/kii/cloud/engine/Constants.java).


- The SDK javadoc is not automtatic visible in your eclipse project, you need to attach the JavaDoc([doc folder](https://github.com/kii-dev-jenkins/KiiFileStorageSampleApp/tree/master/doc)) to the Kii Cloud SDK jar file ([lib folder](https://github.com/kii-dev-jenkins/KiiFileStorageSampleApp/tree/master/libs)). 

- [Java API documenatation](http://static.kii.com/devportal/production/docs/storage/)

All of the samples are licensed under the [Apache 2.0 license](http://www.apache.org/licenses/LICENSE-2.0), so feel free to use any of the code in your own applications as needed!
