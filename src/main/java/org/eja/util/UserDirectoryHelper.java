package org.eja.util;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.HashMap;
import java.util.Map;

public final class UserDirectoryHelper {
    public static Map<String, String> createUserMap(String userId, String password, String displayName) {
        Map<String, String> userEntries = new HashMap<>();

        userEntries.put("password", password);
        userEntries.put("vm-password", userId);

        userEntries.put("toll_allow", "domestic,international,local");
        userEntries.put("accountcode", userId);
        userEntries.put("user_context", "default");
        userEntries.put("effective_caller_id_name", displayName);
        userEntries.put("effective_caller_id_number", userId);
        userEntries.put("outbound_caller_id_name", displayName);
        userEntries.put("outbound_caller_id_number", "$${outbound_caller_id}");
        userEntries.put("callgroup", "techsupport");

        return userEntries;
    }

    public static String createUserString(String userId, String password, String displayName) {
        return "\t" + "<user id=\"" + userId + "\">" +
                "\n\t\t" + "<params>" +
                "\n\t\t\t\t" + "<param name=\"password\" value=\"" + password + "\"/>" +
                "\n\t\t\t\t" + "<param name=\"vm-password\" value=\"" + userId + "\"/>" +
                "\n\t\t" + "</params>" +
                "\n\t\t" + "<variables>" +
                "\n\t\t\t\t" + "<variable name=\"toll_allow\" value=\"domestic,international,local\"/>" +
                "\n\t\t\t\t" + "<variable name=\"accountcode\" value=\"" + userId + "\"/>" +
                "\n\t\t\t\t" + "<variable name=\"user_context\" value=\"default\"/>" +
                "\n\t\t\t\t" + "<variable name=\"effective_caller_id_name\" value=\"" + displayName + "\"/>" +
                "\n\t\t\t\t" + "<variable name=\"effective_caller_id_number\" value=\"" + userId + "\"/>" +
                "\n\t\t\t\t" + "<variable name=\"outbound_caller_id_name\" value=\"" + displayName + "\"/>" +
                "\n\t\t\t\t" + "<variable name=\"outbound_caller_id_number\" value=\"$${outbound_caller_id}\"/>" +
                "\n\t\t\t\t" + "<variable name=\"callgroup\" value=\"techsupport\"/>" +
                "\n\t\t" + "</variables>" +
                "\n\t" + "</user>";
    }

    public static Map<String, String> parseUserEntries(NodeList nodeList, String nodeEntryName) {
        Map<String, String> entries = new HashMap<>();

        for (int j = 0; j < nodeList.getLength(); j++) {
            Node rootNode = nodeList.item(j);

            if (rootNode.getNodeType() == Node.ELEMENT_NODE) {
                Element rootNodeElement = (Element) rootNode;
                NodeList nodes = rootNodeElement.getElementsByTagName(nodeEntryName);

                for (int k = 0; k < nodes.getLength(); k++) {
                    Node entryNode = nodes.item(k);

                    if (entryNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element entryElement = (Element) entryNode;
                        String name = entryElement.getAttribute("name");
                        String value = entryElement.getAttribute("value");

                        entries.put(name, value);
                    }
                }
            }
        }

        return entries;
    }
}
