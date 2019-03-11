package org.openmrs.module.eptsreports.reporting.calculation.pvls;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.openmrs.Concept;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.PatientState;
import org.openmrs.calculation.patient.PatientCalculationContext;
import org.openmrs.calculation.result.CalculationResultMap;
import org.openmrs.calculation.result.ListResult;
import org.openmrs.calculation.result.SimpleResult;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.reporting.calculation.AbstractPatientCalculation;
import org.openmrs.module.eptsreports.reporting.calculation.EptsCalculations;
import org.openmrs.module.eptsreports.reporting.utils.EptsCalculationUtils;
import org.openmrs.module.reporting.common.TimeQualifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Calculates for patient eligibility to be breastfeeding
 *
 * @return CalculationResultMap
 */
@Component
public class BreastfeedingDateCalculation extends AbstractPatientCalculation {

  @Autowired private HivMetadata hivMetadata;

  @Override
  public CalculationResultMap evaluate(
      Collection<Integer> cohort,
      Map<String, Object> parameterValues,
      PatientCalculationContext context) {
    CalculationResultMap resultMap = new CalculationResultMap();

    Location location = (Location) context.getFromCache("location");

    Concept viralLoadConcept = this.hivMetadata.getHivViralLoadConcept();
    EncounterType labEncounterType = this.hivMetadata.getMisauLaboratorioEncounterType();
    EncounterType adultFollowup = this.hivMetadata.getAdultoSeguimentoEncounterType();
    EncounterType childFollowup = this.hivMetadata.getARVPediatriaSeguimentoEncounterType();

    Concept breastfeedingConcept = this.hivMetadata.getBreastfeeding();
    Concept yes = this.hivMetadata.getYesConcept();
    Concept criteriaForHivStart = this.hivMetadata.getCriteriaForArtStart();
    Concept priorDeliveryDate = this.hivMetadata.getPriorDeliveryDateConcept();
    Date oneYearBefore = EptsCalculationUtils.addMonths(context.getNow(), -12);

    // get female patients only
    Set<Integer> femaleCohort = EptsCalculationUtils.female(cohort, context);

    CalculationResultMap lactatingMap =
        EptsCalculations.getObs(
            breastfeedingConcept,
            femaleCohort,
            Arrays.asList(location),
            Arrays.asList(yes),
            TimeQualifier.LAST,
            null,
            context);

    CalculationResultMap criteriaHivStartMap =
        EptsCalculations.getObs(
            criteriaForHivStart,
            femaleCohort,
            Arrays.asList(location),
            Arrays.asList(breastfeedingConcept),
            TimeQualifier.FIRST,
            null,
            context);

    CalculationResultMap deliveryDateMap =
        EptsCalculations.getObs(
            priorDeliveryDate,
            femaleCohort,
            Arrays.asList(location),
            null,
            TimeQualifier.ANY,
            null,
            context);

    CalculationResultMap patientStateMap =
        EptsCalculations.allPatientStates(
            femaleCohort,
            location,
            this.hivMetadata.getPatientIsBreastfeedingWorkflowState(),
            context);

    CalculationResultMap lastVl =
        EptsCalculations.lastObs(
            Arrays.asList(labEncounterType, adultFollowup, childFollowup),
            viralLoadConcept,
            location,
            oneYearBefore,
            context.getNow(),
            femaleCohort,
            context);

    for (Integer pId : femaleCohort) {

      Date resultantDate = null;

      Obs lastVlObs = EptsCalculationUtils.resultForPatient(lastVl, pId);

      if (lastVlObs != null && lastVlObs.getObsDatetime() != null) {
        Date lastVlDate = lastVlObs.getObsDatetime();

        ListResult patientResult = (ListResult) patientStateMap.get(pId);

        Obs lactattingObs = EptsCalculationUtils.resultForPatient(lactatingMap, pId);
        Obs criteriaHivObs = EptsCalculationUtils.resultForPatient(criteriaHivStartMap, pId);
        ListResult deliveryDateResult = (ListResult) deliveryDateMap.get(pId);
        List<Obs> deliveryDateObsList =
            EptsCalculationUtils.extractResultValues(deliveryDateResult);
        List<PatientState> patientStateList =
            EptsCalculationUtils.extractResultValues(patientResult);

        // get a list of all eligible dates
        List<Date> allEligibleDates =
            Arrays.asList(
                this.isLactating(lastVlDate, lactattingObs),
                this.hasHIVStartDate(lastVlDate, criteriaHivObs),
                this.hasDeliveryDate(lastVlDate, deliveryDateObsList),
                this.isInBreastFeedingInProgram(lastVlDate, patientStateList));

        // have a resultant list of dates
        List<Date> resultantList = new ArrayList<>();
        if (allEligibleDates.size() > 0) {
          for (Date breastfeedingDate : allEligibleDates) {
            if (breastfeedingDate != null) {
              resultantList.add(breastfeedingDate);
            }
          }
        }
        if (resultantList.size() > 0) {
          Collections.sort(resultantList);
          // then pick the most recent entry, which is the last one
          resultantDate = resultantList.get(resultantList.size() - 1);
        }
      }
      resultMap.put(pId, new SimpleResult(resultantDate, this));
    }
    return resultMap;
  }

  private Date hasDeliveryDate(Date lastVlDate, List<Obs> deliveryDateObsList) {
    Date deliveryDate = null;
    for (Obs deliverDateObs : deliveryDateObsList) {
      if (deliverDateObs.getValueDatetime() != null
          && this.isInBreastFeedingViralLoadRange(lastVlDate, deliverDateObs.getValueDatetime())) {
        deliveryDate = deliverDateObs.getValueDatetime();
      }
    }
    return deliveryDate;
  }

  private Date isLactating(Date lastVlDate, Obs lactantObs) {
    Date lactatingDate = null;
    if (lactantObs != null
        && this.isInBreastFeedingViralLoadRange(
            lastVlDate, lactantObs.getEncounter().getEncounterDatetime())) {
      lactatingDate = lactantObs.getEncounter().getEncounterDatetime();
    }
    return lactatingDate;
  }

  private Date hasHIVStartDate(Date lastVlDate, Obs hivStartDateObs) {
    Date hivStartDate = null;
    if (hivStartDateObs != null
        && this.isInBreastFeedingViralLoadRange(
            lastVlDate, hivStartDateObs.getEncounter().getEncounterDatetime())) {
      hivStartDate = hivStartDateObs.getEncounter().getEncounterDatetime();
    }
    return hivStartDate;
  }

  private Date isInBreastFeedingInProgram(Date lastVlDate, List<PatientState> patientStateList) {
    Date inProgramDate = null;
    if (!patientStateList.isEmpty()) {
      for (PatientState patientState : patientStateList) {
        if (this.isInBreastFeedingViralLoadRange(lastVlDate, patientState.getStartDate())) {
          inProgramDate = patientState.getStartDate();
        }
      }
    }
    return inProgramDate;
  }

  private boolean isInBreastFeedingViralLoadRange(Date viralLoadDate, Date breastFeedingDate) {

    Date startDate = EptsCalculationUtils.addMonths(viralLoadDate, -18);
    return breastFeedingDate.compareTo(startDate) >= 0
        && breastFeedingDate.compareTo(viralLoadDate) <= 0;
  }
}
