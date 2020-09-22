package liquibase.parser.core.xml;

import liquibase.parser.LiquibaseParser;
import liquibase.parser.NamespaceDetails;
import liquibase.serializer.LiquibaseSerializable;
import liquibase.serializer.LiquibaseSerializer;
import liquibase.serializer.core.xml.XMLChangeLogSerializer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StandardNamespaceDetails implements NamespaceDetails {

    public static final String GENERIC_EXTENSION_XSD = "http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-ext.xsd";
    private final Pattern standardUrlPattern;
    private final Pattern oldStandardUrlPattern;

    public StandardNamespaceDetails() {
        standardUrlPattern = Pattern.compile("http://www.liquibase.org/xml/ns/dbchangelog/(dbchangelog-[\\d\\.]+.xsd)");
        oldStandardUrlPattern = Pattern.compile("http://www.liquibase.org/xml/ns/migrator/(dbchangelog-[\\d\\.]+.xsd)");
    }

    
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    
    public boolean supports(LiquibaseSerializer serializer, String namespaceOrUrl) {
        return serializer instanceof XMLChangeLogSerializer;
    }

    
    public boolean supports(LiquibaseParser parser, String namespaceOrUrl) {
        return parser instanceof XMLChangeLogSAXParser;
    }

    
    public String getShortName(String namespaceOrUrl) {
        if (namespaceOrUrl.equals(LiquibaseSerializable.STANDARD_CHANGELOG_NAMESPACE)) {
            return "";
        }
        return "ext";
    }

    
    public String getSchemaUrl(String namespaceOrUrl) {
        if (namespaceOrUrl.equals(LiquibaseSerializable.STANDARD_CHANGELOG_NAMESPACE)) {
            return "http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-" + XMLChangeLogSAXParser.getSchemaVersion() + ".xsd";
        }
        return GENERIC_EXTENSION_XSD;
    }

    
    public String getLocalPath(String namespaceOrUrl) {

        if (namespaceOrUrl.equals(GENERIC_EXTENSION_XSD)) {
            return "liquibase/parser/core/xml/dbchangelog-ext.xsd";
        }
        Matcher matcher = standardUrlPattern.matcher(namespaceOrUrl);
        if (matcher.matches()) {
            return "liquibase/parser/core/xml/"+matcher.group(1);
        }

        matcher = oldStandardUrlPattern.matcher(namespaceOrUrl);
        if (matcher.matches()) {
            return "liquibase/parser/core/xml/"+matcher.group(1);
        }

        return null;
    }
}
