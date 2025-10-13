package com.prati.projetomercado.service;

import com.prati.projetomercado.dto.request.NfcePatchRequest;
import com.prati.projetomercado.dto.request.NfceRequest;
import com.prati.projetomercado.dto.response.NfceResponse;
import com.prati.projetomercado.dto.response.StatesResponse;

import java.util.List;

public interface NfceService {
    NfceResponse findByAccessKey(String accessToken, String accessKey);

    List<NfceResponse> findAllByUser(String accessToken);

    StatesResponse getAvailableStates(String accessToken);

    NfceResponse createFromLink(String accessToken, String url);

    NfceResponse createManually(String accessToken, NfceRequest nfceData);

    NfceResponse update(String accessToken, String accessKey, NfceRequest nfceData);

    NfceResponse partialUpdate(String accessToken, String accessKey, NfcePatchRequest patchData);

    void delete(String accessToken, String accessKey);
}
