package org.openmrs.module.eptsreports.reporting.intergrated.utils;

import java.util.Date;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.Location;
import org.openmrs.api.context.Context;
import org.openmrs.module.eptsreports.reporting.library.cohorts.TXTBCohortQueries;
import org.openmrs.module.reporting.cohort.EvaluatedCohort;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.service.CohortDefinitionService;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This is a sample FGH database cohortDefinitions test, run FGH instance and then set
 * ~/.OpenMRS/openmrs-runtime.properties appropriately. Tested against PEPFAR MER 2.1 report results
 * with the same parameter values in here while running in eclipse
 */
@Ignore
public class TXTBCohortDefinitionsFGHLiveTest extends BaseModuleContextSensitiveTest {
  @Autowired private TXTBCohortQueries txTbCohortQueries;

  /** @see BaseContextSensitiveTest#useInMemoryDatabase() */
  @Override
  public Boolean useInMemoryDatabase() {
    /*
     * ensure ~/.OpenMRS/openmrs-runtime.properties exists with your properties
     * such as; connection.username=openmrs
     * connection.url=jdbc:mysql://127.0.0.1:3316/openmrs
     * connection.password=wTV.Tpp0|Q&c
     */
    return false;
  }

  @Before
  public void initialize() throws Exception {
    Context.authenticate("admin", "eSaude123");
  }

  private void addParameters(CohortDefinition cd) {
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));
  }

  private void setParameters(
      Date startDate, Date endDate, Location location, EvaluationContext context) {
    context.addParameterValue("startDate", startDate);
    context.addParameterValue("endDate", endDate);
    context.addParameterValue("location", location);
  }

  /** evaluate for location#103 from 06/feb/2013 to 06/mar/2019 */
  private EvaluatedCohort evaluateCodedObsCohortDefinition(CohortDefinition cd)
      throws EvaluationException {
    addParameters(cd);
    EvaluationContext context = new EvaluationContext();
    context.addParameterValue("onOrAfter", DateUtil.getDateTime(2013, 02, 06));
    context.addParameterValue("onOrBefore", DateUtil.getDateTime(2019, 03, 06));
    context.addParameterValue("locationList", Context.getLocationService().getLocation(103));
    return Context.getService(CohortDefinitionService.class).evaluate(cd, context);
  }

  /** evaluate for location#103 from 06/feb/2013 to 06/mar/2019 */
  private EvaluatedCohort evaluateCohortDefinition(CohortDefinition cd) throws EvaluationException {
    addParameters(cd);
    EvaluationContext context = new EvaluationContext();

    setParameters(
        DateUtil.getDateTime(2013, 02, 06),
        DateUtil.getDateTime(2019, 03, 06),
        Context.getLocationService().getLocation(103),
        context);
    return Context.getService(CohortDefinitionService.class).evaluate(cd, context);
  }

  private EvaluatedCohort evaluateCohortDefinition(
      CohortDefinition cd, Date startDate, Date endDate, Location location)
      throws EvaluationException {
    addParameters(cd);
    EvaluationContext context = new EvaluationContext();

    setParameters(startDate, endDate, location, context);
    return Context.getService(CohortDefinitionService.class).evaluate(cd, context);
  }

  @Test
  public void getPatientsWhoCameOutOfARVTreatmentProgram() throws EvaluationException {
    // TODO remove startDate
    EvaluatedCohort result =
        evaluateCohortDefinition(txTbCohortQueries.getPatientsWhoCameOutOfARVTreatmentProgram());
    Assert.assertEquals(108, result.size());
  }

  @Test
  public void getInARTProgram() throws EvaluationException {
    // TODO remove startDate
    EvaluatedCohort result = evaluateCohortDefinition(txTbCohortQueries.getInARTProgram());
    Assert.assertEquals(407, result.size());
  }

  @Test
  public void everyTimeARVTreatedFinal() throws EvaluationException {
    // TODO remove startDate
    EvaluatedCohort result =
        evaluateCodedObsCohortDefinition(txTbCohortQueries.everyTimeARVTreatedFinal());
    Assert.assertEquals(322, result.size());
  }

  @Test
  public void anyTimeARVTreatmentFinalPeriod() throws EvaluationException {
    // TODO remove startDate
    EvaluatedCohort result =
        evaluateCohortDefinition(txTbCohortQueries.anyTimeARVTreatmentFinalPeriod());
    Assert.assertEquals(422, result.size());
  }

  @Test
  public void artTargetHistoricalStartUsingEndDate() throws EvaluationException {
    // TODO remove startDate
    EvaluatedCohort result =
        evaluateCohortDefinition(txTbCohortQueries.artTargetHistoricalStartUsingEndDate());
    Assert.assertEquals(350, result.size());
  }

  @Test
  public void tbTreatmentStartUsingEndDate() throws EvaluationException {
    // TODO remove startDate
    EvaluatedCohort result =
        evaluateCohortDefinition(txTbCohortQueries.tbTreatmentStartUsingEndDate());
    Assert.assertEquals(3, result.size());
  }

  @Test
  public void getCurrentlyInARTTreatmentCompositionFinalPeriod() throws EvaluationException {
    // TODO remove startDate
    EvaluatedCohort result =
        evaluateCohortDefinition(
            txTbCohortQueries.getCurrentlyInARTTreatmentCompositionFinalPeriod());
    Assert.assertEquals(314, result.size());
  }

  @Test
  public void getPatientsInARTWithoutAbandonedNotification() throws EvaluationException {
    // TODO remove startDate
    EvaluatedCohort result =
        evaluateCohortDefinition(txTbCohortQueries.getPatientsInARTWithoutAbandonedNotification());
    Assert.assertEquals(23, result.size());
  }

  @Test
  public void arTTreatmentFromPharmacy() throws EvaluationException {
    // TODO remove startDate
    EvaluatedCohort result = evaluateCohortDefinition(txTbCohortQueries.arTTreatmentFromPharmacy());
    Assert.assertEquals(414, result.size());
  }

  @Test
  public void getNotifiedTBPatientsAtARVService() throws EvaluationException {
    EvaluatedCohort result =
        evaluateCohortDefinition(txTbCohortQueries.getNotifiedTBPatientsAtARVService());
    Assert.assertEquals(0, result.size());
  }

  @Test
  public void notifiedTbPatientsOnARTService() throws EvaluationException {
    EvaluatedCohort result =
        evaluateCohortDefinition(txTbCohortQueries.notifiedTbPatientsOnARTService());
    Assert.assertEquals(5, result.size());
  }

  @Test
  public void notifiedTbPatientsOnARVNewStarting() throws EvaluationException {
    EvaluatedCohort result =
        evaluateCohortDefinition(txTbCohortQueries.notifiedTbPatientsOnARVNewStarting());
    Assert.assertEquals(5, result.size());
  }

  @Test
  public void patientsWhoScreenTbNegativeOrPositive() throws EvaluationException {
    EvaluatedCohort result =
        evaluateCohortDefinition(txTbCohortQueries.patientsWhoScreenTbNegativeOrPositive());
    Assert.assertEquals(342, result.size());
    System.out.println(result.getCommaSeparatedPatientIds());
  }

  @Test
  public void patientsOnARTWhoScreenedTBPositiveForAPeriod() throws EvaluationException {
    EvaluatedCohort result =
        evaluateCohortDefinition(txTbCohortQueries.patientsOnARTWhoScreenedTBPositiveForAPeriod());
    Assert.assertEquals(0, result.size());
  }

  @Test
  public void patientsOnARTWhoScreenedTBPositiveForAPeriodLocation379() throws EvaluationException {
    EvaluatedCohort result =
        evaluateCohortDefinition(
            txTbCohortQueries.patientsOnARTWhoScreenedTBPositiveForAPeriod(),
            DateUtil.getDateTime(2013, 02, 06),
            DateUtil.getDateTime(2019, 03, 06),
            Context.getLocationService().getLocation(379));
    Assert.assertEquals(0, result.size());
  }

  @Test
  public void patientsOnARTWhoScreenedTBNegativeForAPeriod() throws EvaluationException {
    EvaluatedCohort result =
        evaluateCohortDefinition(txTbCohortQueries.patientsOnARTWhoScreenedTBNegativeForAPeriod());
    Assert.assertEquals(3, result.size());
  }

  @Test
  public void patientsWithPositiveTBTrialNotTransferredOut() throws EvaluationException {
    EvaluatedCohort result =
        evaluateCohortDefinition(txTbCohortQueries.patientsWithPositiveTBTrialNotTransferredOut());
    Assert.assertEquals(15, result.size());
  }

  @Test
  public void patientsWithNegativeTBTrialNotTransferredOut() throws EvaluationException {
    EvaluatedCohort result =
        evaluateCohortDefinition(txTbCohortQueries.patientsWithNegativeTBTrialNotTransferredOut());
    Assert.assertEquals(324, result.size());
  }

  @Test
  public void getNonVoidedPatientsAtProgramStateWithinStartAndEndDatesAtLocation()
      throws EvaluationException {
    EvaluatedCohort result =
        evaluateCohortDefinition(
            txTbCohortQueries.getNonVoidedPatientsAtProgramStateWithinStartAndEndDatesAtLocation());
    Assert.assertEquals(356, result.size());
  }

  @Test
  public void getPatientsTransferredFromARTTreatment() throws EvaluationException {
    EvaluatedCohort result =
        evaluateCohortDefinition(txTbCohortQueries.getPatientsTransferredFromARTTreatment());
    Assert.assertEquals(65, result.size());
  }

  @Test
  public void getNotifiedTBTreatmentPatientsOnART() throws EvaluationException {
    EvaluatedCohort result =
        evaluateCohortDefinition(txTbCohortQueries.getNotifiedTBTreatmentPatientsOnART());
    Assert.assertEquals(9, result.size());
  }

  @Test
  public void getInTBProgram() throws EvaluationException {
    EvaluatedCohort result = evaluateCohortDefinition(txTbCohortQueries.getInTBProgram());
    Assert.assertEquals(7, result.size());
  }

  @Test
  public void codedNegativeTbScreening() throws EvaluationException {
    EvaluatedCohort result =
        evaluateCodedObsCohortDefinition(txTbCohortQueries.codedNegativeTbScreening());
    Assert.assertEquals(413, result.size());
  }

  @Test
  public void codedPositiveTbScreening() throws EvaluationException {
    EvaluatedCohort result =
        evaluateCodedObsCohortDefinition(txTbCohortQueries.codedPositiveTbScreening());
    Assert.assertEquals(18, result.size());
  }
}
