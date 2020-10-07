# Editable AEM components

This is a project template for AEM-based applications. It intends to prove the concept for the editable component as illustrated on [Stop developing composite view AEM components with "Editable Component"​](https://www.linkedin.com/pulse/stop-developing-composite-view-aem-components-editable-vimal-kumar)

**The code provided is not production-ready and is only for proofing the concept. 

## Editable Component

The main parts of the editable component are:

* [Editable Component](/ui.apps/src/main/content/jcr_root/apps/editable-components/components/editablecomponent) 
* [JCRUtil](/core/src/main/java/com/editable/components/core/jcr/JcrUtil.java) 
* [EditableComponentsListener](/core/src/main/java/com/editable/components/core/listeners/EditableComponentsListener.java)

## How to build

To build all the modules run in the project root directory the following command with Maven 3:

    mvn clean install

To build all the modules and deploy the `all` package to a local instance of AEM, run in the project root directory the following command:

    mvn clean install -PautoInstallSinglePackage

Or to deploy it to a publish instance, run

    mvn clean install -PautoInstallSinglePackagePublish

Or alternatively

    mvn clean install -PautoInstallSinglePackage -Daem.port=4503

Or to deploy only the bundle to the author, run

    mvn clean install -PautoInstallBundle

Or to deploy only a single content package, run in the sub-module directory (i.e `ui.apps`)

    mvn clean install -PautoInstallPackage

## Maven settings

The project comes with the auto-public repository configured. To setup the repository in your Maven settings, refer to:

    http://helpx.adobe.com/experience-manager/kb/SetUpTheAdobeMavenRepository.html
