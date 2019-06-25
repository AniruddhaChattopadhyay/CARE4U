package com.kevalpatel2106.sample.Model;

public class Allergy {

    private String Name,Cause,Medicines;

    public Allergy()
    {

    }

    public Allergy(String Name, String Cause, String Medicines) {
        this.Name = Name;
        this.Cause = Cause;
        this.Medicines = Medicines;
    }

    public String getName() {
        return Name;
    }

    public void setName(String Name) {
        this.Name = Name;
    }

    public String getCause() {
        return Cause;
    }

    public void setCause(String Cause) {
        this.Cause = Cause;
    }

    public String getMedicines() {
        return Medicines;
    }

    public void setMedicines(String Medicines) {
        this.Medicines = Medicines;
    }
}
