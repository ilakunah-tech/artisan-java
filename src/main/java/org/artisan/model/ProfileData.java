package org.artisan.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Full roast profile data — parity with Python ProfileData TypedDict (atypes.py).
 * Used for .alog JSON serialisation/deserialisation via Jackson.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProfileData {

    // ── Version metadata ──────────────────────────────────────────────────────
    @JsonProperty("recording_version")  private String recordingVersion;
    @JsonProperty("recording_revision") private String recordingRevision;
    @JsonProperty("recording_build")    private String recordingBuild;
    @JsonProperty("version")   private String version;
    @JsonProperty("revision")  private String revision;
    @JsonProperty("build")     private String build;
    @JsonProperty("artisan_os")          private String artisanOs;
    @JsonProperty("artisan_os_version")  private String artisanOsVersion;
    @JsonProperty("artisan_os_arch")     private String artisanOsArch;

    // ── Mode ─────────────────────────────────────────────────────────────────
    @JsonProperty("mode")       private String  mode;
    @JsonProperty("viewerMode") private Boolean viewerMode;

    // ── Flavor wheel ─────────────────────────────────────────────────────────
    @JsonProperty("flavors")                  private List<Double>  flavors                 = new ArrayList<>();
    @JsonProperty("flavors_total_correction") private Double        flavorsTotalCorrection;
    @JsonProperty("flavorlabels")             private List<String>  flavorlabels            = new ArrayList<>();
    @JsonProperty("flavorstartangle")         private Double        flavorstartangle;
    @JsonProperty("flavoraspect")             private Double        flavoraspect;

    // ── Identification ───────────────────────────────────────────────────────
    @JsonProperty("title")  private String title;
    @JsonProperty("locale") private String locale;

    // ── Plus / Cloud ─────────────────────────────────────────────────────────
    @JsonProperty("plus_store")             private String       plusStore;
    @JsonProperty("plus_store_label")       private String       plusStoreLabel;
    @JsonProperty("plus_coffee")            private String       plusCoffee;
    @JsonProperty("plus_coffee_label")      private String       plusCoffeeLabel;
    @JsonProperty("plus_blend_label")       private String       plusBlendLabel;
    @JsonProperty("plus_blend_spec_labels") private List<String> plusBlendSpecLabels        = new ArrayList<>();
    @JsonProperty("plus_sync_record_hash")  private String       plusSyncRecordHash;

    // ── Beans / Weight / Volume / Density ────────────────────────────────────
    @JsonProperty("beans")            private String       beans;
    /** [weightIn, weightOut, unit-string] */
    @JsonProperty("weight")           private List<Object> weight                = new ArrayList<>();
    /** [volumeIn, volumeOut, unit-string] */
    @JsonProperty("volume")           private List<Object> volume                = new ArrayList<>();
    /** [value, unit-string] */
    @JsonProperty("density")          private List<Object> density               = new ArrayList<>();
    /** [value, unit-string] */
    @JsonProperty("density_roasted")  private List<Object> densityRoasted        = new ArrayList<>();
    @JsonProperty("defects_weight")   private Double       defectsWeight;

    // ── Roaster info ─────────────────────────────────────────────────────────
    @JsonProperty("roastertype")    private String  roastertype;
    @JsonProperty("roastersize")    private Double  roastersize;
    @JsonProperty("roasterheating") private Integer roasterheating;
    @JsonProperty("machinesetup")   private String  machinesetup;
    @JsonProperty("operator")       private String  operator;
    @JsonProperty("organization")   private String  organization;
    @JsonProperty("drumspeed")      private String  drumspeed;

    // ── Defect flags ─────────────────────────────────────────────────────────
    @JsonProperty("heavyFC")   private Boolean heavyFC;
    @JsonProperty("lowFC")     private Boolean lowFC;
    @JsonProperty("lightCut")  private Boolean lightCut;
    @JsonProperty("darkCut")   private Boolean darkCut;
    @JsonProperty("drops")     private Boolean drops;
    @JsonProperty("oily")      private Boolean oily;
    @JsonProperty("uneven")    private Boolean uneven;
    @JsonProperty("tipping")   private Boolean tipping;
    @JsonProperty("scorching") private Boolean scorching;
    @JsonProperty("divots")    private Boolean divots;

    // ── Color ────────────────────────────────────────────────────────────────
    @JsonProperty("whole_color")  private Double wholeColor;
    @JsonProperty("ground_color") private Double groundColor;
    @JsonProperty("color_system") private String colorSystem;

    // ── Volume calc weight helpers ────────────────────────────────────────────
    @JsonProperty("volumeCalcWeightIn")  private String volumeCalcWeightIn;
    @JsonProperty("volumeCalcWeightOut") private String volumeCalcWeightOut;

    // ── Date / time ──────────────────────────────────────────────────────────
    @JsonProperty("roastdate")      private String  roastdate;
    @JsonProperty("roastisodate")   private String  roastisodate;
    @JsonProperty("roasttime")      private String  roasttime;
    @JsonProperty("roastepoch")     private Long    roastepoch;
    @JsonProperty("roasttzoffset")  private Integer roasttzoffset;

    // ── Batch ────────────────────────────────────────────────────────────────
    @JsonProperty("roastbatchnr")     private Integer roastbatchnr;
    @JsonProperty("roastbatchprefix") private String  roastbatchprefix;
    @JsonProperty("roastbatchpos")    private Integer roastbatchpos;
    @JsonProperty("roastUUID")        private String  roastUUID;
    @JsonProperty("scheduleID")       private String  scheduleID;
    @JsonProperty("scheduleDate")     private String  scheduleDate;

    // ── Bean size ────────────────────────────────────────────────────────────
    @JsonProperty("beansize")     private String beansize;
    @JsonProperty("beansize_min") private String beansizeMin;
    @JsonProperty("beansize_max") private String beansizeMax;

    // ── Curve data ───────────────────────────────────────────────────────────
    @JsonProperty("timex") private List<Double> timex = new ArrayList<>();
    @JsonProperty("temp1") private List<Double> temp1 = new ArrayList<>();
    @JsonProperty("temp2") private List<Double> temp2 = new ArrayList<>();
    /** ΔET / RoR(ET), optional. */
    private List<Double> delta1 = new ArrayList<>();
    /** ΔBT / RoR(BT), optional. */
    private List<Double> delta2 = new ArrayList<>();

    // ── Events ───────────────────────────────────────────────────────────────
    @JsonProperty("specialevents")        private List<Integer> specialevents        = new ArrayList<>();
    @JsonProperty("specialeventstype")    private List<Integer> specialeventstype    = new ArrayList<>();
    @JsonProperty("specialeventsvalue")   private List<Double>  specialeventsvalue   = new ArrayList<>();
    @JsonProperty("specialeventsStrings") private List<String>  specialeventsStrings = new ArrayList<>();
    @JsonProperty("default_etypes")       private List<Boolean> defaultEtypes        = new ArrayList<>();
    @JsonProperty("default_etypes_set")   private List<Integer> defaultEtypesSet     = new ArrayList<>();
    @JsonProperty("etypes")               private List<String>  etypes               = new ArrayList<>();

    /** [0]=CHARGE, [1]=DRY_END, [2]=FC_START, [3]=FC_END, [4]=SCs, [5]=SCe, [6]=DROP, [7]=COOL. -1 or 0 = not set. */
    @JsonProperty("timeindex") private List<Integer> timeindex = new ArrayList<>();

    // ── Notes ────────────────────────────────────────────────────────────────
    @JsonProperty("cuppingnotes")  private String cuppingnotes;
    @JsonProperty("roastingnotes") private String roastingnotes;

    // ── Axis / view config ───────────────────────────────────────────────────
    @JsonProperty("phases") private List<Integer> phases = new ArrayList<>();
    @JsonProperty("zmax")   private Integer zmax;
    @JsonProperty("zmin")   private Integer zmin;
    @JsonProperty("ymax")   private Integer ymax;
    @JsonProperty("ymin")   private Integer ymin;
    @JsonProperty("xmin")   private Double  xmin;
    @JsonProperty("xmax")   private Double  xmax;

    // ── Ambient conditions ───────────────────────────────────────────────────
    @JsonProperty("ambientTemp")        private Double ambientTemp;
    @JsonProperty("ambient_humidity")   private Double ambientHumidity;
    @JsonProperty("ambient_pressure")   private Double ambientPressure;
    @JsonProperty("moisture_greens")    private Double moistureGreens;
    @JsonProperty("greens_temp")        private Double greensTemp;
    @JsonProperty("moisture_roasted")   private Double moistureRoasted;

    // ── Extra devices ────────────────────────────────────────────────────────
    @JsonProperty("extradevices")          private List<Integer>       extradevices         = new ArrayList<>();
    @JsonProperty("extraname1")            private List<String>        extraname1           = new ArrayList<>();
    @JsonProperty("extraname2")            private List<String>        extraname2           = new ArrayList<>();
    @JsonProperty("extratimex")            private List<List<Double>>  extratimex           = new ArrayList<>();
    @JsonProperty("extratemp1")            private List<List<Double>>  extratemp1           = new ArrayList<>();
    @JsonProperty("extratemp2")            private List<List<Double>>  extratemp2           = new ArrayList<>();
    @JsonProperty("extramathexpression1")  private List<String>        extramathexpression1 = new ArrayList<>();
    @JsonProperty("extramathexpression2")  private List<String>        extramathexpression2 = new ArrayList<>();
    @JsonProperty("extradevicecolor1")     private List<String>        extradevicecolor1    = new ArrayList<>();
    @JsonProperty("extradevicecolor2")     private List<String>        extradevicecolor2    = new ArrayList<>();
    @JsonProperty("extraLCDvisibility1")   private List<Boolean>       extraLCDvisibility1  = new ArrayList<>();
    @JsonProperty("extraLCDvisibility2")   private List<Boolean>       extraLCDvisibility2  = new ArrayList<>();
    @JsonProperty("extraCurveVisibility1") private List<Boolean>       extraCurveVisibility1= new ArrayList<>();
    @JsonProperty("extraCurveVisibility2") private List<Boolean>       extraCurveVisibility2= new ArrayList<>();
    @JsonProperty("extraDelta1")           private List<Boolean>       extraDelta1          = new ArrayList<>();
    @JsonProperty("extraDelta2")           private List<Boolean>       extraDelta2          = new ArrayList<>();
    @JsonProperty("extraFill1")            private List<Integer>       extraFill1           = new ArrayList<>();
    @JsonProperty("extraFill2")            private List<Integer>       extraFill2           = new ArrayList<>();
    @JsonProperty("extramarkersizes1")     private List<Double>        extramarkersizes1    = new ArrayList<>();
    @JsonProperty("extramarkersizes2")     private List<Double>        extramarkersizes2    = new ArrayList<>();
    @JsonProperty("extramarkers1")         private List<String>        extramarkers1        = new ArrayList<>();
    @JsonProperty("extramarkers2")         private List<String>        extramarkers2        = new ArrayList<>();
    @JsonProperty("extralinewidths1")      private List<Double>        extralinewidths1     = new ArrayList<>();
    @JsonProperty("extralinewidths2")      private List<Double>        extralinewidths2     = new ArrayList<>();
    @JsonProperty("extralinestyles1")      private List<String>        extralinestyles1     = new ArrayList<>();
    @JsonProperty("extralinestyles2")      private List<String>        extralinestyles2     = new ArrayList<>();
    @JsonProperty("extradrawstyles1")      private List<String>        extradrawstyles1     = new ArrayList<>();
    @JsonProperty("extradrawstyles2")      private List<String>        extradrawstyles2     = new ArrayList<>();
    @JsonProperty("extraNoneTempHint1")    private List<Boolean>       extraNoneTempHint1   = new ArrayList<>();
    @JsonProperty("extraNoneTempHint2")    private List<Boolean>       extraNoneTempHint2   = new ArrayList<>();

    // ── External programs ────────────────────────────────────────────────────
    @JsonProperty("externalprogram")    private String externalprogram;
    @JsonProperty("externaloutprogram") private String externaloutprogram;

    // ── Alarms ───────────────────────────────────────────────────────────────
    @JsonProperty("alarmsetlabel")    private String        alarmsetlabel;
    @JsonProperty("alarmflag")        private List<Integer> alarmflag        = new ArrayList<>();
    @JsonProperty("alarmguard")       private List<Integer> alarmguard       = new ArrayList<>();
    @JsonProperty("alarmnegguard")    private List<Integer> alarmnegguard    = new ArrayList<>();
    @JsonProperty("alarmtime")        private List<Integer> alarmtime        = new ArrayList<>();
    @JsonProperty("alarmoffset")      private List<Integer> alarmoffset      = new ArrayList<>();
    @JsonProperty("alarmcond")        private List<Integer> alarmcond        = new ArrayList<>();
    @JsonProperty("alarmsource")      private List<Integer> alarmsource      = new ArrayList<>();
    @JsonProperty("alarmtemperature") private List<Double>  alarmtemperature = new ArrayList<>();
    @JsonProperty("alarmaction")      private List<Integer> alarmaction      = new ArrayList<>();
    @JsonProperty("alarmbeep")        private List<Integer> alarmbeep        = new ArrayList<>();
    @JsonProperty("alarmstrings")     private List<String>  alarmstrings     = new ArrayList<>();

    // ── Background ───────────────────────────────────────────────────────────
    @JsonProperty("backgroundpath") private String backgroundpath;
    @JsonProperty("backgroundUUID") private String backgroundUUID;

    // ── Sampling ─────────────────────────────────────────────────────────────
    @JsonProperty("samplinginterval") private Double samplingInterval;

    // ── PID / Ramp-Soak ──────────────────────────────────────────────────────
    @JsonProperty("svLabel")        private String       svLabel;
    @JsonProperty("svValues")       private List<Double> svValues       = new ArrayList<>();
    @JsonProperty("svRamps")        private List<Integer>svRamps        = new ArrayList<>();
    @JsonProperty("svSoaks")        private List<Integer>svSoaks        = new ArrayList<>();
    @JsonProperty("svActions")      private List<Integer>svActions      = new ArrayList<>();
    @JsonProperty("svBeeps")        private List<Boolean>svBeeps        = new ArrayList<>();
    @JsonProperty("svDescriptions") private List<String> svDescriptions = new ArrayList<>();
    @JsonProperty("pidKp")                private Double  pidKp;
    @JsonProperty("pidKi")                private Double  pidKi;
    @JsonProperty("pidKd")                private Double  pidKd;
    @JsonProperty("pidPsetpointWeight")   private Double  pidPsetpointWeight;
    @JsonProperty("pidDsetpointWeight")   private Double  pidDsetpointWeight;
    @JsonProperty("pidSource")            private Integer pidSource;
    @JsonProperty("svLookahead")          private Integer svLookahead;
    @JsonProperty("ramp_lookahead")       private Integer rampLookahead;
    @JsonProperty("pidKp1")               private Double  pidKp1;
    @JsonProperty("pidKi1")               private Double  pidKi1;
    @JsonProperty("pidKd1")               private Double  pidKd1;
    @JsonProperty("pidKp2")               private Double  pidKp2;
    @JsonProperty("pidKi2")               private Double  pidKi2;
    @JsonProperty("pidKd2")               private Double  pidKd2;
    @JsonProperty("pidSchedule0")         private Double  pidSchedule0;
    @JsonProperty("pidSchedule1")         private Double  pidSchedule1;
    @JsonProperty("pidSchedule2")         private Double  pidSchedule2;
    @JsonProperty("gain_scheduling")          private Boolean gainScheduling;
    @JsonProperty("gain_scheduling_on_SV")    private Boolean gainSchedulingOnSV;
    @JsonProperty("gain_scheduling_quadratic")private Boolean gainSchedulingQuadratic;

    // ── Devices list ─────────────────────────────────────────────────────────
    @JsonProperty("devices")   private List<String>  devices   = new ArrayList<>();
    @JsonProperty("elevation") private Integer       elevation;

    // ── Computed ─────────────────────────────────────────────────────────────
    @JsonProperty("computed") private ComputedProfileInformation computed;

    // ── Annotation positions ──────────────────────────────────────────────────
    @JsonProperty("anno_positions")  private List<List<Double>> annoPositions  = new ArrayList<>();
    @JsonProperty("flag_positions")  private List<List<Double>> flagPositions  = new ArrayList<>();
    @JsonProperty("legendloc_pos")   private List<Double>       legendlocPos   = new ArrayList<>();

    // ── Energy loads ─────────────────────────────────────────────────────────
    @JsonProperty("loadlabels")           private List<String>  loadlabels          = new ArrayList<>();
    @JsonProperty("loadratings")          private List<Double>  loadratings         = new ArrayList<>();
    @JsonProperty("ratingunits")          private List<Integer> ratingunits         = new ArrayList<>();
    @JsonProperty("sourcetypes")          private List<Integer> sourcetypes         = new ArrayList<>();
    @JsonProperty("load_etypes")          private List<Integer> loadEtypes          = new ArrayList<>();
    @JsonProperty("presssure_percents")   private List<Boolean> presssurePercents   = new ArrayList<>();
    @JsonProperty("loadevent_zeropcts")   private List<Integer> loadeventZeropcts   = new ArrayList<>();
    @JsonProperty("loadevent_hundpcts")   private List<Integer> loadeventHundpcts   = new ArrayList<>();
    @JsonProperty("meterlabels")          private List<String>  meterlabels         = new ArrayList<>();
    @JsonProperty("meterunits")           private List<Integer> meterunits          = new ArrayList<>();
    @JsonProperty("metersources")         private List<Integer> metersources        = new ArrayList<>();
    @JsonProperty("meterfuels")           private List<Integer> meterfuels          = new ArrayList<>();
    @JsonProperty("co2kg_per_btu")        private List<Double>  co2kgPerBtu         = new ArrayList<>();
    @JsonProperty("biogas_co2_reduction") private Double        biogasCo2Reduction;

    // ── Preheat / BBP / Cooling energy durations ──────────────────────────────
    @JsonProperty("preheatDuration")          private Integer       preheatDuration;
    @JsonProperty("preheatenergies")          private List<Double>  preheatenergies       = new ArrayList<>();
    @JsonProperty("betweenbatchDuration")     private Integer       betweenbatchDuration;
    @JsonProperty("betweenbatchenergies")     private List<Double>  betweenbatchenergies  = new ArrayList<>();
    @JsonProperty("coolingDuration")          private Integer       coolingDuration;
    @JsonProperty("coolingenergies")          private List<Double>  coolingenergies       = new ArrayList<>();
    @JsonProperty("betweenbatch_after_preheat")private Boolean      betweenbatchAfterPreheat;
    @JsonProperty("electricEnergyMix")        private Integer       electricEnergyMix;
    @JsonProperty("gasMix")                   private Integer       gasMix;
    @JsonProperty("meterreads")               private List<List<Double>> meterreads       = new ArrayList<>();

    // ── BBP session data ──────────────────────────────────────────────────────
    @JsonProperty("bbp_begin")                private String        bbpBegin;
    @JsonProperty("bbp_time_added_from_prev") private Double        bbpTimeAddedFromPrev;
    @JsonProperty("bbp_endroast_epoch_msec")  private Long          bbpEndroastEpochMsec;
    @JsonProperty("bbp_endevents")            private List<List<Object>> bbpEndevents    = new ArrayList<>();
    @JsonProperty("bbp_dropevents")           private List<List<Object>> bbpDropevents   = new ArrayList<>();
    @JsonProperty("bbp_dropbt")               private Double        bbpDropbt;
    @JsonProperty("bbp_dropet")               private Double        bbpDropet;
    @JsonProperty("bbp_drop_to_end")          private Double        bbpDropToEnd;

    // ── Getters / Setters ────────────────────────────────────────────────────

    public String getRecordingVersion() { return recordingVersion; }
    public void setRecordingVersion(String v) { this.recordingVersion = v; }

    public String getRecordingRevision() { return recordingRevision; }
    public void setRecordingRevision(String v) { this.recordingRevision = v; }

    public String getRecordingBuild() { return recordingBuild; }
    public void setRecordingBuild(String v) { this.recordingBuild = v; }

    public String getVersion() { return version; }
    public void setVersion(String v) { this.version = v; }

    public String getRevision() { return revision; }
    public void setRevision(String v) { this.revision = v; }

    public String getBuild() { return build; }
    public void setBuild(String v) { this.build = v; }

    public String getArtisanOs() { return artisanOs; }
    public void setArtisanOs(String v) { this.artisanOs = v; }

    public String getArtisanOsVersion() { return artisanOsVersion; }
    public void setArtisanOsVersion(String v) { this.artisanOsVersion = v; }

    public String getArtisanOsArch() { return artisanOsArch; }
    public void setArtisanOsArch(String v) { this.artisanOsArch = v; }

    public String getMode() { return mode; }
    public void setMode(String v) { this.mode = v; }

    public Boolean getViewerMode() { return viewerMode; }
    public void setViewerMode(Boolean v) { this.viewerMode = v; }

    public List<Double> getFlavors() { return flavors; }
    public void setFlavors(List<Double> v) { this.flavors = v != null ? v : new ArrayList<>(); }

    public Double getFlavorsTotalCorrection() { return flavorsTotalCorrection; }
    public void setFlavorsTotalCorrection(Double v) { this.flavorsTotalCorrection = v; }

    public List<String> getFlavorlabels() { return flavorlabels; }
    public void setFlavorlabels(List<String> v) { this.flavorlabels = v != null ? v : new ArrayList<>(); }

    public Double getFlavorstartangle() { return flavorstartangle; }
    public void setFlavorstartangle(Double v) { this.flavorstartangle = v; }

    public Double getFlavoraspect() { return flavoraspect; }
    public void setFlavoraspect(Double v) { this.flavoraspect = v; }

    public String getTitle() { return title; }
    public void setTitle(String v) { this.title = v; }

    public String getLocale() { return locale; }
    public void setLocale(String v) { this.locale = v; }

    public String getPlusStore() { return plusStore; }
    public void setPlusStore(String v) { this.plusStore = v; }

    public String getPlusStoreLabel() { return plusStoreLabel; }
    public void setPlusStoreLabel(String v) { this.plusStoreLabel = v; }

    public String getPlusCoffee() { return plusCoffee; }
    public void setPlusCoffee(String v) { this.plusCoffee = v; }

    public String getPlusCoffeeLabel() { return plusCoffeeLabel; }
    public void setPlusCoffeeLabel(String v) { this.plusCoffeeLabel = v; }

    public String getPlusBlendLabel() { return plusBlendLabel; }
    public void setPlusBlendLabel(String v) { this.plusBlendLabel = v; }

    public List<String> getPlusBlendSpecLabels() { return plusBlendSpecLabels; }
    public void setPlusBlendSpecLabels(List<String> v) { this.plusBlendSpecLabels = v != null ? v : new ArrayList<>(); }

    public String getPlusSyncRecordHash() { return plusSyncRecordHash; }
    public void setPlusSyncRecordHash(String v) { this.plusSyncRecordHash = v; }

    public String getBeans() { return beans; }
    public void setBeans(String v) { this.beans = v; }

    public List<Object> getWeight() { return weight; }
    public void setWeight(List<Object> v) { this.weight = v != null ? v : new ArrayList<>(); }

    public List<Object> getVolume() { return volume; }
    public void setVolume(List<Object> v) { this.volume = v != null ? v : new ArrayList<>(); }

    public List<Object> getDensity() { return density; }
    public void setDensity(List<Object> v) { this.density = v != null ? v : new ArrayList<>(); }

    public List<Object> getDensityRoasted() { return densityRoasted; }
    public void setDensityRoasted(List<Object> v) { this.densityRoasted = v != null ? v : new ArrayList<>(); }

    public Double getDefectsWeight() { return defectsWeight; }
    public void setDefectsWeight(Double v) { this.defectsWeight = v; }

    public String getRoastertype() { return roastertype; }
    public void setRoastertype(String v) { this.roastertype = v; }

    public Double getRoastersize() { return roastersize; }
    public void setRoastersize(Double v) { this.roastersize = v; }

    public Integer getRoasterheating() { return roasterheating; }
    public void setRoasterheating(Integer v) { this.roasterheating = v; }

    public String getMachinesetup() { return machinesetup; }
    public void setMachinesetup(String v) { this.machinesetup = v; }

    public String getOperator() { return operator; }
    public void setOperator(String v) { this.operator = v; }

    public String getOrganization() { return organization; }
    public void setOrganization(String v) { this.organization = v; }

    public String getDrumspeed() { return drumspeed; }
    public void setDrumspeed(String v) { this.drumspeed = v; }

    public Boolean getHeavyFC() { return heavyFC; }
    public void setHeavyFC(Boolean v) { this.heavyFC = v; }

    public Boolean getLowFC() { return lowFC; }
    public void setLowFC(Boolean v) { this.lowFC = v; }

    public Boolean getLightCut() { return lightCut; }
    public void setLightCut(Boolean v) { this.lightCut = v; }

    public Boolean getDarkCut() { return darkCut; }
    public void setDarkCut(Boolean v) { this.darkCut = v; }

    public Boolean getDrops() { return drops; }
    public void setDrops(Boolean v) { this.drops = v; }

    public Boolean getOily() { return oily; }
    public void setOily(Boolean v) { this.oily = v; }

    public Boolean getUneven() { return uneven; }
    public void setUneven(Boolean v) { this.uneven = v; }

    public Boolean getTipping() { return tipping; }
    public void setTipping(Boolean v) { this.tipping = v; }

    public Boolean getScorching() { return scorching; }
    public void setScorching(Boolean v) { this.scorching = v; }

    public Boolean getDivots() { return divots; }
    public void setDivots(Boolean v) { this.divots = v; }

    public Double getWholeColor() { return wholeColor; }
    public void setWholeColor(Double v) { this.wholeColor = v; }

    public Double getGroundColor() { return groundColor; }
    public void setGroundColor(Double v) { this.groundColor = v; }

    public String getColorSystem() { return colorSystem; }
    public void setColorSystem(String v) { this.colorSystem = v; }

    public String getVolumeCalcWeightIn() { return volumeCalcWeightIn; }
    public void setVolumeCalcWeightIn(String v) { this.volumeCalcWeightIn = v; }

    public String getVolumeCalcWeightOut() { return volumeCalcWeightOut; }
    public void setVolumeCalcWeightOut(String v) { this.volumeCalcWeightOut = v; }

    public String getRoastdate() { return roastdate; }
    public void setRoastdate(String v) { this.roastdate = v; }

    public String getRoastisodate() { return roastisodate; }
    public void setRoastisodate(String v) { this.roastisodate = v; }

    public String getRoasttime() { return roasttime; }
    public void setRoasttime(String v) { this.roasttime = v; }

    public Long getRoastepoch() { return roastepoch; }
    public void setRoastepoch(Long v) { this.roastepoch = v; }

    public Integer getRoasttzoffset() { return roasttzoffset; }
    public void setRoasttzoffset(Integer v) { this.roasttzoffset = v; }

    public Integer getRoastbatchnr() { return roastbatchnr; }
    public void setRoastbatchnr(Integer v) { this.roastbatchnr = v; }

    public String getRoastbatchprefix() { return roastbatchprefix; }
    public void setRoastbatchprefix(String v) { this.roastbatchprefix = v; }

    public Integer getRoastbatchpos() { return roastbatchpos; }
    public void setRoastbatchpos(Integer v) { this.roastbatchpos = v; }

    public String getRoastUUID() { return roastUUID; }
    public void setRoastUUID(String v) { this.roastUUID = v; }

    public String getScheduleID() { return scheduleID; }
    public void setScheduleID(String v) { this.scheduleID = v; }

    public String getScheduleDate() { return scheduleDate; }
    public void setScheduleDate(String v) { this.scheduleDate = v; }

    public String getBeansize() { return beansize; }
    public void setBeansize(String v) { this.beansize = v; }

    public String getBeansizeMin() { return beansizeMin; }
    public void setBeansizeMin(String v) { this.beansizeMin = v; }

    public String getBeansizeMax() { return beansizeMax; }
    public void setBeansizeMax(String v) { this.beansizeMax = v; }

    public List<Double> getTimex() { return timex; }
    public void setTimex(List<Double> v) { this.timex = v != null ? v : new ArrayList<>(); }

    public List<Double> getTemp1() { return temp1; }
    public void setTemp1(List<Double> v) { this.temp1 = v != null ? v : new ArrayList<>(); }

    public List<Double> getTemp2() { return temp2; }
    public void setTemp2(List<Double> v) { this.temp2 = v != null ? v : new ArrayList<>(); }

    public List<Double> getDelta1() { return delta1; }
    public void setDelta1(List<Double> v) { this.delta1 = v != null ? v : new ArrayList<>(); }

    public List<Double> getDelta2() { return delta2; }
    public void setDelta2(List<Double> v) { this.delta2 = v != null ? v : new ArrayList<>(); }

    public List<Integer> getSpecialevents() { return specialevents; }
    public void setSpecialevents(List<Integer> v) { this.specialevents = v != null ? v : new ArrayList<>(); }

    public List<Integer> getSpecialeventstype() { return specialeventstype; }
    public void setSpecialeventstype(List<Integer> v) { this.specialeventstype = v != null ? v : new ArrayList<>(); }

    public List<Double> getSpecialeventsvalue() { return specialeventsvalue; }
    public void setSpecialeventsvalue(List<Double> v) { this.specialeventsvalue = v != null ? v : new ArrayList<>(); }

    public List<String> getSpecialeventsStrings() { return specialeventsStrings; }
    public void setSpecialeventsStrings(List<String> v) { this.specialeventsStrings = v != null ? v : new ArrayList<>(); }

    public List<Boolean> getDefaultEtypes() { return defaultEtypes; }
    public void setDefaultEtypes(List<Boolean> v) { this.defaultEtypes = v != null ? v : new ArrayList<>(); }

    public List<Integer> getDefaultEtypesSet() { return defaultEtypesSet; }
    public void setDefaultEtypesSet(List<Integer> v) { this.defaultEtypesSet = v != null ? v : new ArrayList<>(); }

    public List<String> getEtypes() { return etypes; }
    public void setEtypes(List<String> v) { this.etypes = v != null ? v : new ArrayList<>(); }

    public List<Integer> getTimeindex() { return timeindex; }
    public void setTimeindex(List<Integer> v) { this.timeindex = v != null ? v : new ArrayList<>(); }

    public String getCuppingnotes() { return cuppingnotes; }
    public void setCuppingnotes(String v) { this.cuppingnotes = v; }

    public String getRoastingnotes() { return roastingnotes; }
    public void setRoastingnotes(String v) { this.roastingnotes = v; }

    public List<Integer> getPhases() { return phases; }
    public void setPhases(List<Integer> v) { this.phases = v != null ? v : new ArrayList<>(); }

    public Integer getZmax() { return zmax; }
    public void setZmax(Integer v) { this.zmax = v; }

    public Integer getZmin() { return zmin; }
    public void setZmin(Integer v) { this.zmin = v; }

    public Integer getYmax() { return ymax; }
    public void setYmax(Integer v) { this.ymax = v; }

    public Integer getYmin() { return ymin; }
    public void setYmin(Integer v) { this.ymin = v; }

    public Double getXmin() { return xmin; }
    public void setXmin(Double v) { this.xmin = v; }

    public Double getXmax() { return xmax; }
    public void setXmax(Double v) { this.xmax = v; }

    public Double getAmbientTemp() { return ambientTemp; }
    public void setAmbientTemp(Double v) { this.ambientTemp = v; }

    public Double getAmbientHumidity() { return ambientHumidity; }
    public void setAmbientHumidity(Double v) { this.ambientHumidity = v; }

    public Double getAmbientPressure() { return ambientPressure; }
    public void setAmbientPressure(Double v) { this.ambientPressure = v; }

    public Double getMoistureGreens() { return moistureGreens; }
    public void setMoistureGreens(Double v) { this.moistureGreens = v; }

    public Double getGreensTemp() { return greensTemp; }
    public void setGreensTemp(Double v) { this.greensTemp = v; }

    public Double getMoistureRoasted() { return moistureRoasted; }
    public void setMoistureRoasted(Double v) { this.moistureRoasted = v; }

    public List<Integer> getExtradevices() { return extradevices; }
    public void setExtradevices(List<Integer> v) { this.extradevices = v != null ? v : new ArrayList<>(); }

    public List<String> getExtraname1() { return extraname1; }
    public void setExtraname1(List<String> v) { this.extraname1 = v != null ? v : new ArrayList<>(); }

    public List<String> getExtraname2() { return extraname2; }
    public void setExtraname2(List<String> v) { this.extraname2 = v != null ? v : new ArrayList<>(); }

    public List<List<Double>> getExtratimex() { return extratimex; }
    public void setExtratimex(List<List<Double>> v) { this.extratimex = v != null ? v : new ArrayList<>(); }

    public List<List<Double>> getExtratemp1() { return extratemp1; }
    public void setExtratemp1(List<List<Double>> v) { this.extratemp1 = v != null ? v : new ArrayList<>(); }

    public List<List<Double>> getExtratemp2() { return extratemp2; }
    public void setExtratemp2(List<List<Double>> v) { this.extratemp2 = v != null ? v : new ArrayList<>(); }

    public List<String> getExtramathexpression1() { return extramathexpression1; }
    public void setExtramathexpression1(List<String> v) { this.extramathexpression1 = v != null ? v : new ArrayList<>(); }

    public List<String> getExtramathexpression2() { return extramathexpression2; }
    public void setExtramathexpression2(List<String> v) { this.extramathexpression2 = v != null ? v : new ArrayList<>(); }

    public List<String> getExtradevicecolor1() { return extradevicecolor1; }
    public void setExtradevicecolor1(List<String> v) { this.extradevicecolor1 = v != null ? v : new ArrayList<>(); }

    public List<String> getExtradevicecolor2() { return extradevicecolor2; }
    public void setExtradevicecolor2(List<String> v) { this.extradevicecolor2 = v != null ? v : new ArrayList<>(); }

    public List<Boolean> getExtraLCDvisibility1() { return extraLCDvisibility1; }
    public void setExtraLCDvisibility1(List<Boolean> v) { this.extraLCDvisibility1 = v != null ? v : new ArrayList<>(); }

    public List<Boolean> getExtraLCDvisibility2() { return extraLCDvisibility2; }
    public void setExtraLCDvisibility2(List<Boolean> v) { this.extraLCDvisibility2 = v != null ? v : new ArrayList<>(); }

    public List<Boolean> getExtraCurveVisibility1() { return extraCurveVisibility1; }
    public void setExtraCurveVisibility1(List<Boolean> v) { this.extraCurveVisibility1 = v != null ? v : new ArrayList<>(); }

    public List<Boolean> getExtraCurveVisibility2() { return extraCurveVisibility2; }
    public void setExtraCurveVisibility2(List<Boolean> v) { this.extraCurveVisibility2 = v != null ? v : new ArrayList<>(); }

    public List<Boolean> getExtraDelta1() { return extraDelta1; }
    public void setExtraDelta1(List<Boolean> v) { this.extraDelta1 = v != null ? v : new ArrayList<>(); }

    public List<Boolean> getExtraDelta2() { return extraDelta2; }
    public void setExtraDelta2(List<Boolean> v) { this.extraDelta2 = v != null ? v : new ArrayList<>(); }

    public List<Integer> getExtraFill1() { return extraFill1; }
    public void setExtraFill1(List<Integer> v) { this.extraFill1 = v != null ? v : new ArrayList<>(); }

    public List<Integer> getExtraFill2() { return extraFill2; }
    public void setExtraFill2(List<Integer> v) { this.extraFill2 = v != null ? v : new ArrayList<>(); }

    public List<Double> getExtramarkersizes1() { return extramarkersizes1; }
    public void setExtramarkersizes1(List<Double> v) { this.extramarkersizes1 = v != null ? v : new ArrayList<>(); }

    public List<Double> getExtramarkersizes2() { return extramarkersizes2; }
    public void setExtramarkersizes2(List<Double> v) { this.extramarkersizes2 = v != null ? v : new ArrayList<>(); }

    public List<String> getExtramarkers1() { return extramarkers1; }
    public void setExtramarkers1(List<String> v) { this.extramarkers1 = v != null ? v : new ArrayList<>(); }

    public List<String> getExtramarkers2() { return extramarkers2; }
    public void setExtramarkers2(List<String> v) { this.extramarkers2 = v != null ? v : new ArrayList<>(); }

    public List<Double> getExtralinewidths1() { return extralinewidths1; }
    public void setExtralinewidths1(List<Double> v) { this.extralinewidths1 = v != null ? v : new ArrayList<>(); }

    public List<Double> getExtralinewidths2() { return extralinewidths2; }
    public void setExtralinewidths2(List<Double> v) { this.extralinewidths2 = v != null ? v : new ArrayList<>(); }

    public List<String> getExtralinestyles1() { return extralinestyles1; }
    public void setExtralinestyles1(List<String> v) { this.extralinestyles1 = v != null ? v : new ArrayList<>(); }

    public List<String> getExtralinestyles2() { return extralinestyles2; }
    public void setExtralinestyles2(List<String> v) { this.extralinestyles2 = v != null ? v : new ArrayList<>(); }

    public List<String> getExtradrawstyles1() { return extradrawstyles1; }
    public void setExtradrawstyles1(List<String> v) { this.extradrawstyles1 = v != null ? v : new ArrayList<>(); }

    public List<String> getExtradrawstyles2() { return extradrawstyles2; }
    public void setExtradrawstyles2(List<String> v) { this.extradrawstyles2 = v != null ? v : new ArrayList<>(); }

    public List<Boolean> getExtraNoneTempHint1() { return extraNoneTempHint1; }
    public void setExtraNoneTempHint1(List<Boolean> v) { this.extraNoneTempHint1 = v != null ? v : new ArrayList<>(); }

    public List<Boolean> getExtraNoneTempHint2() { return extraNoneTempHint2; }
    public void setExtraNoneTempHint2(List<Boolean> v) { this.extraNoneTempHint2 = v != null ? v : new ArrayList<>(); }

    public String getExternalprogram() { return externalprogram; }
    public void setExternalprogram(String v) { this.externalprogram = v; }

    public String getExternaloutprogram() { return externaloutprogram; }
    public void setExternaloutprogram(String v) { this.externaloutprogram = v; }

    public String getAlarmsetlabel() { return alarmsetlabel; }
    public void setAlarmsetlabel(String v) { this.alarmsetlabel = v; }

    public List<Integer> getAlarmflag() { return alarmflag; }
    public void setAlarmflag(List<Integer> v) { this.alarmflag = v != null ? v : new ArrayList<>(); }

    public List<Integer> getAlarmguard() { return alarmguard; }
    public void setAlarmguard(List<Integer> v) { this.alarmguard = v != null ? v : new ArrayList<>(); }

    public List<Integer> getAlarmnegguard() { return alarmnegguard; }
    public void setAlarmnegguard(List<Integer> v) { this.alarmnegguard = v != null ? v : new ArrayList<>(); }

    public List<Integer> getAlarmtime() { return alarmtime; }
    public void setAlarmtime(List<Integer> v) { this.alarmtime = v != null ? v : new ArrayList<>(); }

    public List<Integer> getAlarmoffset() { return alarmoffset; }
    public void setAlarmoffset(List<Integer> v) { this.alarmoffset = v != null ? v : new ArrayList<>(); }

    public List<Integer> getAlarmcond() { return alarmcond; }
    public void setAlarmcond(List<Integer> v) { this.alarmcond = v != null ? v : new ArrayList<>(); }

    public List<Integer> getAlarmsource() { return alarmsource; }
    public void setAlarmsource(List<Integer> v) { this.alarmsource = v != null ? v : new ArrayList<>(); }

    public List<Double> getAlarmtemperature() { return alarmtemperature; }
    public void setAlarmtemperature(List<Double> v) { this.alarmtemperature = v != null ? v : new ArrayList<>(); }

    public List<Integer> getAlarmaction() { return alarmaction; }
    public void setAlarmaction(List<Integer> v) { this.alarmaction = v != null ? v : new ArrayList<>(); }

    public List<Integer> getAlarmbeep() { return alarmbeep; }
    public void setAlarmbeep(List<Integer> v) { this.alarmbeep = v != null ? v : new ArrayList<>(); }

    public List<String> getAlarmstrings() { return alarmstrings; }
    public void setAlarmstrings(List<String> v) { this.alarmstrings = v != null ? v : new ArrayList<>(); }

    public String getBackgroundpath() { return backgroundpath; }
    public void setBackgroundpath(String v) { this.backgroundpath = v; }

    public String getBackgroundUUID() { return backgroundUUID; }
    public void setBackgroundUUID(String v) { this.backgroundUUID = v; }

    public Double getSamplingInterval() { return samplingInterval; }
    public void setSamplingInterval(Double v) { this.samplingInterval = v; }

    public String getSvLabel() { return svLabel; }
    public void setSvLabel(String v) { this.svLabel = v; }

    public List<Double> getSvValues() { return svValues; }
    public void setSvValues(List<Double> v) { this.svValues = v != null ? v : new ArrayList<>(); }

    public List<Integer> getSvRamps() { return svRamps; }
    public void setSvRamps(List<Integer> v) { this.svRamps = v != null ? v : new ArrayList<>(); }

    public List<Integer> getSvSoaks() { return svSoaks; }
    public void setSvSoaks(List<Integer> v) { this.svSoaks = v != null ? v : new ArrayList<>(); }

    public List<Integer> getSvActions() { return svActions; }
    public void setSvActions(List<Integer> v) { this.svActions = v != null ? v : new ArrayList<>(); }

    public List<Boolean> getSvBeeps() { return svBeeps; }
    public void setSvBeeps(List<Boolean> v) { this.svBeeps = v != null ? v : new ArrayList<>(); }

    public List<String> getSvDescriptions() { return svDescriptions; }
    public void setSvDescriptions(List<String> v) { this.svDescriptions = v != null ? v : new ArrayList<>(); }

    public Double getPidKp() { return pidKp; }
    public void setPidKp(Double v) { this.pidKp = v; }

    public Double getPidKi() { return pidKi; }
    public void setPidKi(Double v) { this.pidKi = v; }

    public Double getPidKd() { return pidKd; }
    public void setPidKd(Double v) { this.pidKd = v; }

    public Double getPidPsetpointWeight() { return pidPsetpointWeight; }
    public void setPidPsetpointWeight(Double v) { this.pidPsetpointWeight = v; }

    public Double getPidDsetpointWeight() { return pidDsetpointWeight; }
    public void setPidDsetpointWeight(Double v) { this.pidDsetpointWeight = v; }

    public Integer getPidSource() { return pidSource; }
    public void setPidSource(Integer v) { this.pidSource = v; }

    public Integer getSvLookahead() { return svLookahead; }
    public void setSvLookahead(Integer v) { this.svLookahead = v; }

    public Integer getRampLookahead() { return rampLookahead; }
    public void setRampLookahead(Integer v) { this.rampLookahead = v; }

    public Double getPidKp1() { return pidKp1; }
    public void setPidKp1(Double v) { this.pidKp1 = v; }

    public Double getPidKi1() { return pidKi1; }
    public void setPidKi1(Double v) { this.pidKi1 = v; }

    public Double getPidKd1() { return pidKd1; }
    public void setPidKd1(Double v) { this.pidKd1 = v; }

    public Double getPidKp2() { return pidKp2; }
    public void setPidKp2(Double v) { this.pidKp2 = v; }

    public Double getPidKi2() { return pidKi2; }
    public void setPidKi2(Double v) { this.pidKi2 = v; }

    public Double getPidKd2() { return pidKd2; }
    public void setPidKd2(Double v) { this.pidKd2 = v; }

    public Double getPidSchedule0() { return pidSchedule0; }
    public void setPidSchedule0(Double v) { this.pidSchedule0 = v; }

    public Double getPidSchedule1() { return pidSchedule1; }
    public void setPidSchedule1(Double v) { this.pidSchedule1 = v; }

    public Double getPidSchedule2() { return pidSchedule2; }
    public void setPidSchedule2(Double v) { this.pidSchedule2 = v; }

    public Boolean getGainScheduling() { return gainScheduling; }
    public void setGainScheduling(Boolean v) { this.gainScheduling = v; }

    public Boolean getGainSchedulingOnSV() { return gainSchedulingOnSV; }
    public void setGainSchedulingOnSV(Boolean v) { this.gainSchedulingOnSV = v; }

    public Boolean getGainSchedulingQuadratic() { return gainSchedulingQuadratic; }
    public void setGainSchedulingQuadratic(Boolean v) { this.gainSchedulingQuadratic = v; }

    public List<String> getDevices() { return devices; }
    public void setDevices(List<String> v) { this.devices = v != null ? v : new ArrayList<>(); }

    public Integer getElevation() { return elevation; }
    public void setElevation(Integer v) { this.elevation = v; }

    public ComputedProfileInformation getComputed() { return computed; }
    public void setComputed(ComputedProfileInformation v) { this.computed = v; }

    public List<List<Double>> getAnnoPositions() { return annoPositions; }
    public void setAnnoPositions(List<List<Double>> v) { this.annoPositions = v != null ? v : new ArrayList<>(); }

    public List<List<Double>> getFlagPositions() { return flagPositions; }
    public void setFlagPositions(List<List<Double>> v) { this.flagPositions = v != null ? v : new ArrayList<>(); }

    public List<Double> getLegendlocPos() { return legendlocPos; }
    public void setLegendlocPos(List<Double> v) { this.legendlocPos = v != null ? v : new ArrayList<>(); }

    public List<String> getLoadlabels() { return loadlabels; }
    public void setLoadlabels(List<String> v) { this.loadlabels = v != null ? v : new ArrayList<>(); }

    public List<Double> getLoadratings() { return loadratings; }
    public void setLoadratings(List<Double> v) { this.loadratings = v != null ? v : new ArrayList<>(); }

    public List<Integer> getRatingunits() { return ratingunits; }
    public void setRatingunits(List<Integer> v) { this.ratingunits = v != null ? v : new ArrayList<>(); }

    public List<Integer> getSourcetypes() { return sourcetypes; }
    public void setSourcetypes(List<Integer> v) { this.sourcetypes = v != null ? v : new ArrayList<>(); }

    public List<Integer> getLoadEtypes() { return loadEtypes; }
    public void setLoadEtypes(List<Integer> v) { this.loadEtypes = v != null ? v : new ArrayList<>(); }

    public List<Boolean> getPresssurePercents() { return presssurePercents; }
    public void setPresssurePercents(List<Boolean> v) { this.presssurePercents = v != null ? v : new ArrayList<>(); }

    public List<Integer> getLoadeventZeropcts() { return loadeventZeropcts; }
    public void setLoadeventZeropcts(List<Integer> v) { this.loadeventZeropcts = v != null ? v : new ArrayList<>(); }

    public List<Integer> getLoadeventHundpcts() { return loadeventHundpcts; }
    public void setLoadeventHundpcts(List<Integer> v) { this.loadeventHundpcts = v != null ? v : new ArrayList<>(); }

    public List<String> getMeterlabels() { return meterlabels; }
    public void setMeterlabels(List<String> v) { this.meterlabels = v != null ? v : new ArrayList<>(); }

    public List<Integer> getMeterunits() { return meterunits; }
    public void setMeterunits(List<Integer> v) { this.meterunits = v != null ? v : new ArrayList<>(); }

    public List<Integer> getMetersources() { return metersources; }
    public void setMetersources(List<Integer> v) { this.metersources = v != null ? v : new ArrayList<>(); }

    public List<Integer> getMeterfuels() { return meterfuels; }
    public void setMeterfuels(List<Integer> v) { this.meterfuels = v != null ? v : new ArrayList<>(); }

    public List<Double> getCo2kgPerBtu() { return co2kgPerBtu; }
    public void setCo2kgPerBtu(List<Double> v) { this.co2kgPerBtu = v != null ? v : new ArrayList<>(); }

    public Double getBiogasCo2Reduction() { return biogasCo2Reduction; }
    public void setBiogasCo2Reduction(Double v) { this.biogasCo2Reduction = v; }

    public Integer getPreheatDuration() { return preheatDuration; }
    public void setPreheatDuration(Integer v) { this.preheatDuration = v; }

    public List<Double> getPreheatenergies() { return preheatenergies; }
    public void setPreheatenergies(List<Double> v) { this.preheatenergies = v != null ? v : new ArrayList<>(); }

    public Integer getBetweenbatchDuration() { return betweenbatchDuration; }
    public void setBetweenbatchDuration(Integer v) { this.betweenbatchDuration = v; }

    public List<Double> getBetweenbatchenergies() { return betweenbatchenergies; }
    public void setBetweenbatchenergies(List<Double> v) { this.betweenbatchenergies = v != null ? v : new ArrayList<>(); }

    public Integer getCoolingDuration() { return coolingDuration; }
    public void setCoolingDuration(Integer v) { this.coolingDuration = v; }

    public List<Double> getCoolingenergies() { return coolingenergies; }
    public void setCoolingenergies(List<Double> v) { this.coolingenergies = v != null ? v : new ArrayList<>(); }

    public Boolean getBetweenbatchAfterPreheat() { return betweenbatchAfterPreheat; }
    public void setBetweenbatchAfterPreheat(Boolean v) { this.betweenbatchAfterPreheat = v; }

    public Integer getElectricEnergyMix() { return electricEnergyMix; }
    public void setElectricEnergyMix(Integer v) { this.electricEnergyMix = v; }

    public Integer getGasMix() { return gasMix; }
    public void setGasMix(Integer v) { this.gasMix = v; }

    public List<List<Double>> getMeterreads() { return meterreads; }
    public void setMeterreads(List<List<Double>> v) { this.meterreads = v != null ? v : new ArrayList<>(); }

    public String getBbpBegin() { return bbpBegin; }
    public void setBbpBegin(String v) { this.bbpBegin = v; }

    public Double getBbpTimeAddedFromPrev() { return bbpTimeAddedFromPrev; }
    public void setBbpTimeAddedFromPrev(Double v) { this.bbpTimeAddedFromPrev = v; }

    public Long getBbpEndroastEpochMsec() { return bbpEndroastEpochMsec; }
    public void setBbpEndroastEpochMsec(Long v) { this.bbpEndroastEpochMsec = v; }

    public List<List<Object>> getBbpEndevents() { return bbpEndevents; }
    public void setBbpEndevents(List<List<Object>> v) { this.bbpEndevents = v != null ? v : new ArrayList<>(); }

    public List<List<Object>> getBbpDropevents() { return bbpDropevents; }
    public void setBbpDropevents(List<List<Object>> v) { this.bbpDropevents = v != null ? v : new ArrayList<>(); }

    public Double getBbpDropbt() { return bbpDropbt; }
    public void setBbpDropbt(Double v) { this.bbpDropbt = v; }

    public Double getBbpDropet() { return bbpDropet; }
    public void setBbpDropet(Double v) { this.bbpDropet = v; }

    public Double getBbpDropToEnd() { return bbpDropToEnd; }
    public void setBbpDropToEnd(Double v) { this.bbpDropToEnd = v; }

    /** Returns the number of data points (length of timex). */
    public int size() { return timex.size(); }
}
