package Uvis;

@SuppressWarnings({"unused"})
public class CurveMaxRecord {

    private Long id;
    private String curveName;
    private String wavelength;
    private String absorption;

    CurveMaxRecord(Long id, String curveName, Double wavelength, Double abs) {
        this.id = id;
        this.curveName = curveName;
        this.wavelength = wavelength.toString();
        this.absorption = abs.toString();
    }

    /**
     * Всі нижче наведені методи необхідні. Без них не заповнюватиметься форма таблиці
     */
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCurveName() {
        return curveName;
    }

    public void setCurveName(String curveName) {
        this.curveName = curveName;
    }

    public String getWavelength() {
        return wavelength;
    }

    public void setWavelength(Double wavelength) {
        this.wavelength = wavelength.toString();
    }

    public String getAbsorption() {
        return absorption;
    }

    public void setAbsorption(Double absorption) {
        this.absorption = absorption.toString();
    }

}