package ga.nashawygroup.whatsappsender.sender.liseteners;

import java.util.List;

import ga.nashawygroup.whatsappsender.sender.model.WContact;
import ga.nashawygroup.whatsappsender.sender.model.WMessage;

public interface SendMessageListener {
    void finishSendWMessage(List<WContact> contact, List<WMessage> messages);
}
