package com.backbase.stream.compositions.legalentity.core.model;

import com.backbase.stream.legalentity.model.LegalEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
public class LegalEntityResponse {

  private Boolean productChainEnabledFromRequest;
  private final LegalEntity legalEntity;
  private final List<String> membershipAccounts;
  private Map<String, String> additions;
}