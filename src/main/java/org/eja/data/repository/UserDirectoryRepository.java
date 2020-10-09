package org.eja.data.repository;

import lombok.extern.slf4j.Slf4j;
import org.eja.props.FreeSwitchProps;
import org.eja.util.UserDirectoryHelper;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Slf4j
@Component
public class UserDirectoryRepository {
    private final FreeSwitchProps freeSwitchProps;

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public UserDirectoryRepository(FreeSwitchProps freeSwitchProps) {
        this.freeSwitchProps = freeSwitchProps;
    }

    public void save(String userId, String password, String displayName) throws Exception {
        lock.writeLock().lock();

        try (RandomAccessFile file = new RandomAccessFile(freeSwitchProps.getUserDirectoryPath(), "rw")) {
            // Seek backwards and remove last line (</include>)
            long fileLength = file.length() - 1;

            byte fileByte;
            int newLineByte = 10;

            do {
                fileLength -= 1;
                file.seek(fileLength);
                fileByte = file.readByte();
            } while (fileByte != newLineByte);

            file.setLength(fileLength + 1);

            // append created user
            file.writeBytes(UserDirectoryHelper.createUserString(userId, password, displayName));

            // append </include> back again
            file.write(newLineByte);
            file.writeBytes("</include>");
        } finally {
            lock.writeLock().unlock();
        }
    }

    public Map<String, Map<String, String>> getAllUsers() throws Exception {
        Map<String, Map<String, String>> users = new HashMap<>();

        lock.readLock().lock();
        try {
            File file = new File(freeSwitchProps.getUserDirectoryPath());
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

            Document document = documentBuilder.parse(file);

            Element rootInclude = document.getDocumentElement();
            NodeList userNodes = rootInclude.getChildNodes();

            for (int i = 0; i < userNodes.getLength(); i++) {
                Node userNode = userNodes.item(i);

                if (userNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element userElement = (Element) userNode;
                    String id = userElement.getAttribute("id");

                    if (id != null && !id.equals("")) {
                        NodeList paramsNodes = userElement.getElementsByTagName("params");
                        Map<String, String> params = UserDirectoryHelper.parseUserEntries(paramsNodes, "param");

                        NodeList variablesNodes = userElement.getElementsByTagName("variables");
                        Map<String, String> variables = UserDirectoryHelper.parseUserEntries(variablesNodes,
                                "variable");

                        params.putAll(variables);

                        users.put(id, params);
                    }
                }
            }
        } finally {
            lock.readLock().unlock();
        }

        return users;
    }
}
