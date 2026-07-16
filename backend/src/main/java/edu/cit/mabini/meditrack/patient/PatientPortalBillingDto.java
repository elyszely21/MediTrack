package edu.cit.mabini.meditrack.patient;

import edu.cit.mabini.meditrack.billing.BillDto;
import edu.cit.mabini.meditrack.billing.PaymentDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientPortalBillingDto {
    private List<BillDto> bills;
    private List<PaymentDto> payments;
}

