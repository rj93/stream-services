package com.backbase.stream.contact;

import com.backbase.dbs.contact.api.service.v2.model.ContactsBulkPostRequestBody;
import com.backbase.dbs.contact.api.service.v2.model.ContactsBulkPostResponseBody;
import com.backbase.stream.worker.model.StreamTask;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Data
public class ContactsTask extends StreamTask {

    private ContactsBulkPostRequestBody data;
    private ContactsBulkPostResponseBody response;

    public ContactsTask(String unitOfWorkId, ContactsBulkPostRequestBody data) {
        super(unitOfWorkId);
        this.data = data;
    }

    @Override
    public String getName() {
        return "contact";
    }
}
