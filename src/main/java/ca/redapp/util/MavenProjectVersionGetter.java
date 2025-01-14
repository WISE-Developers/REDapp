package ca.redapp.util;

import java.io.IOException;
import java.nio.file.Path;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class MavenProjectVersionGetter {

    private MavenProjectVersionGetter() {
        // empty on purpose
    }

    /**
     * Get the version of the current project.
     *
     * @return version string
     */
    public static String getCurrentProjectVersion() {
        return getProjectVersion(Path.of("pom.xml"));
    }

    /**
     * Get the version of the current project as specified for the project itself or for its parent.
     *
     * @return version string
     */
    public static String getVersionOfCurrentProjectOrParent()  {
        return getVersionOfProjectOrParent(Path.of("pom.xml"));
    }

    /**
     * Get the version as specified for the project itself or for its parent
     *
     * @param pomFile path to {@code pom.xml} to get the version from
     * @return version string
     */
    public static String getVersionOfProjectOrParent(final Path pomFile)  {
        final String version = getProjectVersion(pomFile);
        final String parentVersion = getParentVersion(pomFile);
        if (version.isEmpty()) {
            return parentVersion;
        }
        if (parentVersion.isEmpty() || parentVersion.equals(version)) {
            return version;
        }
        //throw new Exception("Inconsistent version information in file {{pom file}}: project version is {{project version}}, while parent version is {{parent version}}.",  pomFile, version, parentVersion);
return null;
    }

    /**
     * Get the version of a given project.
     *
     * @param pomFile path to {@code pom.xml} to get the version from
     * @return version string
     */
    public static String getProjectVersion(final Path pomFile)  {
        return getPropertyOfXmlFile(pomFile, "/project/version");
    }

    /**
     * Get the revision of a parent pom.
     *
     * @param pomFile path to {@code pom.xml} to get the revision from
     * @return revision string
     */
    public static String getProjectRevision(final Path pomFile)  {
        return getPropertyOfXmlFile(pomFile, "/project/properties/revision");
    }

    /**
     * Get the version of the parent project.
     *
     * @param pomFile path to {@code pom.xml} to get the parent version from
     * @return version string
     */
    public static String getParentVersion(final Path pomFile){
        return getPropertyOfXmlFile(pomFile, "/project/parent/version");
    }

    private static String getPropertyOfXmlFile(final Path pomFile, final String propertyXPath) {
        try {
            final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            documentBuilderFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            final Document pom = documentBuilderFactory.newDocumentBuilder().parse(pomFile.toFile());
            final XPath xPath = XPathFactory.newInstance().newXPath();
            return xPath.compile(propertyXPath).evaluate(pom);
        } catch (final XPathExpressionException | SAXException | ParserConfigurationException | IOException exception) {
            return null;
        }
    }

    /**
     * Exception thrown by MavenProjectVersionGetter
     */
    public static class VersionGetterException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        VersionGetterException(final String message, final Exception exception) {
            super(message, exception);
        }

        VersionGetterException(final String message) {
            super(message);
        }
    }
}
