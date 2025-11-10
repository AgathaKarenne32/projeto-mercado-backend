package com.prati.projetomercado.service;

import com.prati.projetomercado.dto.request.NfceFilterRequest;
import com.prati.projetomercado.dto.request.NfcePatchRequest;
import com.prati.projetomercado.dto.request.NfceRequest;
import com.prati.projetomercado.dto.response.NfceResponse;
import com.prati.projetomercado.dto.response.StatesResponse;
import org.springframework.data.domain.Page;

public interface NfceService {
    NfceResponse findByAccessKey(String accessKey);

    Page<NfceResponse> findAllByUser(int page, int size);

    Page<NfceResponse> search(NfceFilterRequest filter, int page, int size);

    StatesResponse getAvailableStates();

    NfceResponse createFromLink(String url);

    NfceResponse createManually(NfceRequest nfceData);

    NfceResponse update(String accessKey, NfceRequest nfceData);

    NfceResponse partialUpdate(String accessKey, NfcePatchRequest patchData);

    void delete(String accessKey);
}
