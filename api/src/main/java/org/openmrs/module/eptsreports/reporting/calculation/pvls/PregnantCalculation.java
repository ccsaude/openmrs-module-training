package org.openmrs.module.eptsreports.reporting.calculation.pvls;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import org.openmrs.api.context.Context;
import org.openmrs.calculation.patient.PatientCalculationContext;
import org.openmrs.calculation.result.CalculationResultMap;
import org.openmrs.module.eptsreports.reporting.calculation.AbstractPatientCalculation;
import org.openmrs.module.eptsreports.reporting.calculation.BooleanResult;
import org.openmrs.module.eptsreports.reporting.utils.EptsCalculationUtils;
import org.springframework.stereotype.Component;

@Component
public class PregnantCalculation extends AbstractPatientCalculation {

  @Override
  public CalculationResultMap evaluate(
      Collection<Integer> cohort,
      Map<String, Object> parameterValues,
      PatientCalculationContext context) {

    CalculationResultMap resultMap = new CalculationResultMap();

    // get female patients only
    Set<Integer> femaleCohort = EptsCalculationUtils.female(cohort, context);

    CalculationResultMap pregnantDateMap =
        calculate(
            Context.getRegisteredComponents(PregnantDateCalculation.class).get(0),
            femaleCohort,
            context);
    CalculationResultMap breastfeedingDateMap =
        calculate(
            Context.getRegisteredComponents(BreastfeedingDateCalculation.class).get(0),
            femaleCohort,
            context);
    for (Integer ptId : femaleCohort) {
      boolean isCandidate = false;
      Date pregnancyDate = (Date) pregnantDateMap.get(ptId).getValue();
      Date breastfeedingDate = (Date) breastfeedingDateMap.get(ptId).getValue();
      if (pregnancyDate != null) {
        isCandidate = true;
      }
      if (breastfeedingDate != null
          && pregnancyDate != null
          && breastfeedingDate.after(pregnancyDate)) {
        isCandidate = false;
      }
      resultMap.put(ptId, new BooleanResult(isCandidate, this));
    }
    return resultMap;
  }
}
