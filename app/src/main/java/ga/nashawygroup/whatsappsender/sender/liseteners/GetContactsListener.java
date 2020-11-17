package ga.nashawygroup.whatsappsender.sender.liseteners;

import java.util.List;

import ga.nashawygroup.whatsappsender.sender.model.WContact;

public interface GetContactsListener {

    void receiveWhatsappContacts(List<WContact> contacts);
}
