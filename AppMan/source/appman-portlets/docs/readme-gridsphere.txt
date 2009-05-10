http://collab-ogce.blogspot.com/2008/08/using-ogce-portlets-in-gridshere-31.html

Using OGCE portlets in Gridshere 3.1 (preliminary)
We had a request to get OGCE portlets running in Gridsphere 3.1. Below is the way to do this manually. We'll try to make an automated version of this with Maven next.

0. Shutdown tomcat.

1. In the ogce-portal-only directory, run the command mvn clean install -f global-config/pom.xml

2. Remove our versions of hibernate, hsqldb, and ehcache (probably these crept in from GS 2.1--I don't think we need them). Specifically,

rm global-config/common/target/cog-common-1.0/lib/hibernate2-OGCE.jar

rm global-config/common/target/cog-common-1.0/lib/hsqldb-1.7.1.jar

rm global-config/common/target/cog-common-1.0/lib/ehcache-0.9.jar

3. Copy the remaining jars into your Tomcat's shared/lib. Something like
cp global-config/common/target/cog-common-1.0/lib/* /Users/mpierce/GridSphere31/apache-tomcat-5.5.20/shared/lib/

4. Copy the OGCE portlet as is from the ogce-portal-only dir. That is, from ogce-portal-only, run "mvn clean install -f portlets/proxymanager-portlet" and then
cp -r portal_deploy/apache-tomcat-5.5.12/webapps/proxymanager-portlet/ /Users/mpierce/GridSphere31/apache-tomcat-5.5.20/webapps/proxymanager-portlet

5. Create empty files named after the portlets for Gridsphere. This used to be done in $CATALINA_HOME/webapps/gridsphere/WEB-INF/CustomPortal/portlets

Now these apparently go in $HOME/.gridsphere, so do this:

touch ~/.gridsphere/portlets/proxymanager-portlet.2

6. Edit the proxymanager's web.xml file to use the correct namespace for the PortletServlet (i.e., remove the "gridlab" section from the full name):

<servlet>
<servlet-name>PortletServlet</servlet-name>
<servlet-class>org.gridsphere.provider.portlet.jsr.PortletServlet</servlet-class>
</servlet>

7. In proxymanager-portlet/WEB-INF/lib, delete gridsphere-ui-tags-2.1.jar

8. Start tomcat and add the portlet using the Gridsphere Layout Manager.
