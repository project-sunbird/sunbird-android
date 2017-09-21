package org.sunbird.models;

import org.ekstep.genieservices.commons.utils.GsonUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DeviceSpecification {

    private String dname;       // Optional user (whoever facilitating the device) defined device name.  Used for helping facilitators track devices and apply filters to reports/dashboards.
    private String dlocname;    // Optional user (whoever facilitating the device) defined device location name. Used for helping facilitators track device location (school name, tuition center name, etc) and apply filters to reports/dashboards.
    private String os;          // OS name and version
    private String make;        // device make and model
    private String id;          // physical device id if available from OS
    private double mem = -1;    // total mem in MB, -1 if unknown
    private double idisk = -1;  // total internal disk in GB, -1 if unknown
    private double edisk = -1;  // total external disk (card) in GB, -1 if unknown
    private double scrn = -1;   // screen size in inches, -1 if unknown
    private String camera;      // "13,1.3", // primary and secondary camera specs
    private String cpu;         // processor name
    private int sims = -1;      // number of sim cards, -1 if unknown
    private List<String> cap;   // enum array representing various capabilities
    // values - "GPS","BT","WIFI","3G","2G","LTE","ACCEL","GYRO","LIGHT","COMP"

    public DeviceSpecification() {
        this.dname = "";
        this.dlocname = "";
        cap = new ArrayList<>();
    }

    public String getDname() {
        return dname;
    }

    public void setDname(String dname) {
        this.dname = dname;
    }

    public String getDlocname() {
        return dlocname;
    }

    public void setDlocname(String dlocname) {
        this.dlocname = dlocname;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getMake() {
        return make;
    }

    public void setMake(String make) {
        this.make = make;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getMem() {
        return mem;
    }

    public void setMem(double mem) {
        this.mem = mem;
    }

    public double getIdisk() {
        return idisk;
    }

    public void setIdisk(double idisk) {
        this.idisk = idisk;
    }

    public double getEdisk() {
        return edisk;
    }

    public void setEdisk(double edisk) {
        this.edisk = edisk;
    }

    public double getScrn() {
        return scrn;
    }

    public void setScrn(double scrn) {
        this.scrn = scrn;
    }

    public String getCamera() {
        return camera;
    }

    public void setCamera(String camera) {
        this.camera = camera;
    }

    public String getCpu() {
        return cpu;
    }

    public void setCpu(String cpu) {
        this.cpu = cpu;
    }

    public int getSims() {
        return sims;
    }

    public void setSims(int sims) {
        this.sims = sims;
    }

    public List<String> getCap() {
        return cap;
    }

    public void setCap(List<String> cap) {
        this.cap = cap;
    }

    public Map<String, Object> toMap() {
        return GsonUtil.fromJson(GsonUtil.toJson(this), Map.class);
    }
}
