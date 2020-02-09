/**
 *  Copyright 2020 Markus Liljergren
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at:
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

// BEGIN:getDefaultParentImports()
/** Default Imports */
import groovy.json.JsonSlurper
import groovy.json.JsonOutput
import java.security.MessageDigest   // Used for MD5 calculations
//import groovy.transform.TypeChecked
//import groovy.transform.TypeCheckingMode
/* Default Parent Imports */
// END:  getDefaultParentImports()


definition(
    name: "Tasmota Device Handler",
    namespace: "tasmota",
    author: "Markus Liljergren (markus-li)",
    description: "Device Manager for Tasmota",
    category: "Convenience",
    //TODO: Replace these icons???
    iconUrl:   "",
    iconX2Url: "",
    iconX3Url: ""
) {
    appSetting "defaultTasmotaPassword"
}

preferences {
     page(name: "mainPage", title: "", install: true, uninstall: true)
     page(name: "deleteDevice")
     page(name: "refreshDevices")
     page(name: "resultPage")
     page(name: "configureTasmotaDevice")
     page(name: "addDevices", title: "Add Tasmota-based Device", content: "addDevices")
     page(name: "manuallyAdd")
     page(name: "manuallyAddConfirm")
}

// https://docs.smartthings.com/en/latest/smartapp-developers-guide/preferences-and-settings.html#preferences-and-settings

/**
 * DEVICE CONFIGURATIONS METHODS (helpers-device-configurations)
 *
 *   Device configurations and functions for using them
 */
TreeMap getDeviceConfigurations() {
    // To add more devices, just add them below ;)
    // Don't forget that BOTH the App and the Universal driver needs to have this configuration.
    // If you add a device and it works well for you, please share your
    // configuration in the Hubitat Community Forum.
    //
    // typeId HAS to be unique
    // 
    // For the rest of the settings, see below to figure it out :P
    List deviceConfigurations = [
        [typeId: 'sonoff-basic-r3', 
         name: 'Sonoff Basic R3',
         module: 1,
         installCommands: [["SetOption81", "0"]],
         deviceLink: 'https://templates.blakadder.com/sonoff_basic_R3.html'],

        [typeId: 'tuyamcu-ce-wf500d-dimmer',
         name: 'TuyaMCU CE Smart Home WF500D Dimmer',
         template: '{"NAME":"CE WF500D","GPIO":[255,255,255,255,255,255,0,0,255,108,255,107,255],"FLAG":0,"BASE":54}',
         installCommands: [["SetOption66", "0"], // Set publishing TuyaReceived to MQTT to DISABLED
         ],
         deviceLink: 'https://templates.blakadder.com/ce_smart_home-WF500D.html'],

        [typeId: 'ce-la-2-w3-wall-outlet',
         name: 'CE Smart Home LA-2-W3 Wall Outlet',
         template: '{"NAME":"CE LA-2-W3","GPIO":[255,255,255,255,157,17,0,0,21,255,255,255,255],"FLAG":15,"BASE":18}',
         installCommands: [],
         deviceLink: 'https://templates.blakadder.com/ce_smart_home_LA-2-W3.html'],

        [typeId: 'ce-lq-2-w3-wall-outlet',
         name: 'CE Smart Home LQ-2-W3 Wall Outlet',
         template: '{"NAME":"CE LQ-2-W3","GPIO":[255,255,255,255,255,17,255,255,21,255,255,255,255],"FLAG":15,"BASE":18}',
         installCommands: [],
         deviceLink: 'https://templates.blakadder.com/ce_smart_home_LQ-2-W3.html'],

        [typeId: 'awp02l-n-plug',
         name: 'AWP02L-N Plug',
         template: '{"NAME":"AWP02L-N","GPIO":[57,0,56,0,0,0,0,0,0,17,0,21,0],"FLAG":1,"BASE":18}',
         installCommands: [],
         deviceLink: 'https://templates.blakadder.com/hugoai_awp02l-n.html'],

        [typeId: 'cyyltf-bifans-j23-plug',
         name: 'CYYLTF BIFANS J23 Plug',
         template: '{"NAME":"CYYLTF J23","GPIO":[56,0,0,0,0,0,0,0,21,17,0,0,0],"FLAG":1,"BASE":18}',
         installCommands: [],
         deviceLink: 'https://templates.blakadder.com/cyyltd_bifans_J23.html'],

        [typeId: 'gosund-wp3-plug',
         name: 'Gosund WP3 Plug',
         template: '{"NAME":"Gosund WP3","GPIO":[0,0,0,0,17,0,0,0,56,57,21,0,0],"FLAG":0,"BASE":18}',
         installCommands: [],
         deviceLink: 'https://templates.blakadder.com/gosund_wp3.html'],

        [typeId: 'sk03-pm-outdoor-plug',
         name: 'SK03 Power Monitor Outdoor Plug',
         template: '{"NAME":"SK03 Outdoor","GPIO":[17,0,0,0,133,132,0,0,131,57,56,21,0],"FLAG":0,"BASE":57}',
         installCommands: [],
         deviceLink: 'https://templates.blakadder.com/SK03_outdoor.html'],

        [typeId: 'aoycocr-x10s-pm-plug',
         name: 'Aoycocr X10S Power Monitor Plug',
         template: '{"NAME":"Aoycocr X10S","GPIO":[56,0,57,0,21,134,0,0,131,17,132,0,0],"FLAG":0,"BASE":45}',
         installCommands: [],
         deviceLink: 'https://templates.blakadder.com/aoycocr_X10S.html'],

        [typeId: 'brilliant-20699-rgbw-bulb',
         name: 'Brilliant 20699 800lm RGBW Bulb',
         template: '{"NAME":"Brilliant20699","GPIO":[0,0,0,0,141,140,0,0,37,142,0,0,0],"FLAG":0,"BASE":18}',
         installCommands: [["WebLog", "2"]],
         deviceLink: 'https://templates.blakadder.com/brilliant_20699.html'],

        [typeId: 'sonoff-sv',
         name: 'Sonoff SV',
         template: '{"NAME":"Sonoff SV","GPIO":[17,255,0,255,255,255,0,0,21,56,255,0,0],"FLAG":1,"BASE":3}',
         installCommands: [],
         deviceLink: 'https://templates.blakadder.com/sonoff_SV.html'],

        [typeId: 'sonoff-th',
         name: 'Sonoff TH',
         template: '{"NAME":"Sonoff TH","GPIO":[17,255,0,255,255,0,0,0,21,56,255,0,0],"FLAG":0,"BASE":4}',
         installCommands: [["TempRes", (tempRes == '' || tempRes == null ? "1" : tempRes)]],
         deviceLink: 'https://templates.blakadder.com/sonoff_TH.html'],

        [typeId: 'sonoff-pow',
         name: 'Sonoff POW',
         template: '{"NAME":"Sonoff Pow","GPIO":[17,0,0,0,0,130,0,0,21,132,133,52,0],"FLAG":0,"BASE":6}',
         installCommands: [["SetOption81", "1"], ["LedPower", "1"], ["LedState", "8"]],
         deviceLink: 'https://templates.blakadder.com/sonoff_Pow.html'],

        [typeId: 'sonoff-s31',
         name: 'Sonoff S31',
         template: '{"NAME":"Sonoff S31","GPIO":[17,145,0,146,0,0,0,0,21,56,0,0,0],"FLAG":0,"BASE":41}',
         installCommands: [["SetOption81", "1"], ["LedPower", "1"], ["LedState", "8"]],
         deviceLink: 'https://templates.blakadder.com/sonoff_S31.html'],

        [typeId: 'kmc-4-pm-plug',
         name: 'KMC 4 Power Monitor Plug',
         template: '{"NAME":"KMC 4 Plug","GPIO":[0,56,0,0,133,132,0,0,130,22,23,21,17],"FLAG":0,"BASE":36}',
         installCommands: [],
         deviceLink: 'https://templates.blakadder.com/kmc-4.html'],

        [typeId: 'awp04l-pm-plug',
         name: 'AWP04L Power Monitor Plug',
         template: '{"NAME":"AWP04L","GPIO":[57,255,255,131,255,134,0,0,21,17,132,56,255],"FLAG":0,"BASE":18}',
         installCommands: [],
         deviceLink: 'https://templates.blakadder.com/awp04l.html'],

        [typeId: 'sonoff-4ch-pro',
         name: 'Sonoff 4CH Pro',
         template: '{"NAME":"Sonoff 4CH Pro","GPIO":[17,255,255,255,23,22,18,19,21,56,20,24,0],"FLAG":0,"BASE":23}',
         installCommands: [],
         deviceLink: 'https://templates.blakadder.com/sonoff_4CH_Pro.html'],

        [typeId: 'unbranded-rgbwwcw-controller-type-1',
         name: 'Unbranded RGBWWCW Controller (Type 1)',
         template: '{"NAME":"CtrlRGBWWCW-T1","GPIO":[17,0,0,0,0,40,0,0,38,39,37,41,0],"FLAG":0,"BASE":18}',
         installCommands: [["WebLog", "2"]],
         deviceLink: ''],
        
        // Tasmota Drivers WITH their own base-file
        [typeId: 'tuyamcu-touch-switch-1-button',
        name: 'TuyaMCU Touch Switch - 1 button',
        module: 54,
        installCommands: [["TuyaMCU", "11,1"], ["TuyaMCU", "12,0"], 
                          ["TuyaMCU", "13,0"], ["TuyaMCU", "14,0"], ["SetOption81", "1"]],
        deviceLink: ''],

        [typeId: 'tuyamcu-touch-switch-2-button',
        name: 'TuyaMCU Touch Switch - 2 buttons',
        module: 54,
        installCommands: [["TuyaMCU", "11,1"], ["TuyaMCU", "12,2"], 
                          ["TuyaMCU", "13,0"], ["TuyaMCU", "14,0"], ["SetOption81", "1"]],
        deviceLink: ''],

        [typeId: 'tuyamcu-touch-switch-3-button',
        name: 'TuyaMCU Touch Switch - 3 buttons',
        module: 54,
        installCommands: [["TuyaMCU", "11,1"], ["TuyaMCU", "12,2"], 
                          ["TuyaMCU", "13,3"], ["TuyaMCU", "14,0"], ["SetOption81", "1"]],
        deviceLink: ''],

        [typeId: 'tuyamcu-touch-switch-4-button',
        name: 'TuyaMCU Touch Switch - 4 buttons',
        module: 54,
        template: '',
        installCommands: [["TuyaMCU", "11,1"], ["TuyaMCU", "12,2"], 
                          ["TuyaMCU", "13,3"], ["TuyaMCU", "14,4"], ["SetOption81", "1"]],
        deviceLink: ''],

        [typeId: 'sonoff-powr2', 
        name: 'Sonoff POW R2',
        template: '{"NAME":"Sonoff Pow R2","GPIO":[17,145,0,146,0,0,0,0,21,56,0,0,0],"FLAG":0,"BASE":43}',
        installCommands: [["SetOption81", "1"], ["LedPower", "1"], ["LedState", "8"]],
        deviceLink: 'https://templates.blakadder.com/sonoff_Pow_R2.html'],

        [typeId: 'sonoff-s20', 
        name: 'Sonoff S20',
        template: '{"NAME":"Sonoff S20","GPIO":[17,255,255,255,0,0,0,0,21,56,0,0,0],"FLAG":0,"BASE":8}',
        installCommands: [["SetOption81", "1"], ["LedPower", "1"], ["LedState", "8"]],
        deviceLink: 'https://templates.blakadder.com/sonoff_S20.html'],

        [typeId: 'sonoff-s26', 
        name: 'Sonoff S26',
        template: '{"NAME":"Sonoff S26","GPIO":[17,255,255,255,0,0,0,0,21,158,0,0,0],"FLAG":0,"BASE":8}',
        installCommands: [["SetOption81", "1"]],
        deviceLink: 'https://templates.blakadder.com/sonoff_S26.html'],

        [typeId: 'sonoff-mini', 
        name: 'Sonoff Mini',
        template: '{"NAME":"Sonoff Mini","GPIO":[17,0,0,0,9,0,0,0,21,56,0,0,255],"FLAG":0,"BASE":1}',
        installCommands: [],
        deviceLink: 'https://templates.blakadder.com/sonoff_mini.html'],

        [typeId: 'sonoff-basic',
        name: 'Sonoff Basic',
        module: 1,
        installCommands: [["SetOption81", "1"]],
        deviceLink: 'https://templates.blakadder.com/sonoff_basic.html'],

        [typeId: 's120-plug' ,
        name: 'S120 USB Charger Plug',
        template: '{"NAME":"S120 Plug","GPIO":[0,0,0,0,0,21,0,0,0,52,90,0,0],"FLAG":0,"BASE":18}',
        installCommands: [["SetOption81", "1"]],
        deviceLink: 'https://templates.blakadder.com/brilliantsmart_20676.html'],

        [typeId: 's120-plug-bmp280' ,
        name: 'S120 USB Charger Plug + BMP280',
        template: '{"NAME":"S120THPPlug","GPIO":[0,6,0,5,0,21,0,0,0,52,90,0,0],"FLAG":0,"BASE":18}',
        installCommands: [["SetOption81", "1"]],
        deviceLink: 'https://templates.blakadder.com/brilliantsmart_20676.html'],

        [typeId: 'brilliantsmart-20676-plug' ,
        name: 'BrilliantSmart 20676 USB Charger Plug',
        template: '{"NAME":"Brilliant20676","GPIO":[0,0,0,0,0,21,0,0,0,52,90,0,0],"FLAG":0,"BASE":18}',
        installCommands: [["SetOption81", "1"]],
        deviceLink: 'https://templates.blakadder.com/brilliantsmart_20676.html'],

        [typeId: 'brilliant-bl20925-pm-plug', 
        name: 'Brilliant Lighting BL20925 PM Plug',
        template: '{"NAME":"BL20925","GPIO":[0,0,0,17,133,132,0,0,131,158,21,0,0],"FLAG":0,"BASE":52}',
        installCommands: [],
        deviceLink: 'https://templates.blakadder.com/brilliant_BL20925.html'],

        [typeId: 'prime-ccrcwfii113pk-plug', 
        name: 'Prime CCRCWFII113PK Plug',
        template: '{"NAME":"PrimeCCRC13PK","GPIO":[0,0,0,0,57,56,0,0,21,122,0,0,0],"FLAG":0,"BASE":18}',
        installCommands: [],
        deviceLink: 'https://templates.blakadder.com/prime_CCRCWFII113PK.html'],

        [typeId: 'tuyamcu-wifi-dimmer', 
        name: 'TuyaMCU Wifi Dimmer',
        module: 54,
        installCommands: [["SetOption66", "0"], // Set publishing TuyaReceived to MQTT to DISABLED
        ],
        deviceLink: ''],

        [typeId: 'unbranded-rgb-controller-with-ir-type-1' ,
        name: 'Unbranded RGB Controller with IR (Type 1)',
        template: '{"NAME":"RGB Controller","GPIO":[0,0,0,0,0,38,0,0,39,51,0,37,0],"FLAG":15,"BASE":18}',
        installCommands: [["WebLog", "2"]],
        deviceLink: ''],

        [typeId: 'sonoff-4ch',
        name: 'Sonoff 4CH',
        template: '{"NAME":"Sonoff 4CH","GPIO":[17,255,255,255,23,22,18,19,21,56,20,24,0],"FLAG":0,"BASE":7}',
        installCommands: [],
       deviceLink: 'https://templates.blakadder.com/sonoff_4CH.html'],

        [typeId: 'tuyamcu-znsn-wifi-curtain-wall-panel',
        comment: 'NOT GENERIC - read the instructions',
        name: 'TuyaMCU ZNSN Wifi Curtain Wall Panel',
        module: 54,
        // TODO: Add special handling for Rule-commands, DON'T use Backlog!
        // TODO: Add the special parsing for this
        installCommands: [["WebLog", "2"],  // A good idea for dimmers
                        ['Mem1', '100'],  // Updated with the current Curtain location
                        ['Mem2', '11'],   // Step for each increase
                        ['Mem3', '1'],    // delay in 10th of a second (1 = 100ms)
                        ['Mem4', '9'],    // Motor startup steps
                        ['Mem5', '1'],    // Extra step when opening
                        ['Delay', '15'],   // Set delay between Backlog commands
                        ['Rule1', 'ON Dimmer#State DO Mem1 %value%; ENDON'],
                        ['Rule1', '+ ON TuyaReceived#Data=55AA00070005650400010277 DO Backlog Var1 %mem1%; Var2 Go; Var5 C; Add1 %mem2%; Sub1 %mem4%; Var4 %mem2%; Event Go; ENDON'],
                        ['Rule1', '+ ON Event#Go DO Backlog Dimmer %var1%; Event %var5%%var1%; Event %var2%2; ENDON'],
                        ['Rule1', '+ ON Event#Go2 DO Backlog Add1 %var4%; Delay %mem3%; Event %var1%; Event %var2%;  ENDON'],
                        ['Rule1', '+ ON Event#O-7 DO Var2 sC; ENDON ON Event#O-8 DO Var2 sC; ENDON ON Event#O-9 DO Var2 sC; ENDON ON Event#O-10 DO Var2 sC; ENDON ON Event#O-11 DO Var2 sC; ENDON'],
                        ['Rule1', '1'],
                        ['Rule2', 'ON TuyaReceived#Data=55AA00070005650400010176 DO Backlog Var1 %mem1%; Var2 Go; Var5 O; Sub1 %mem2%; Add1 %mem4%; Var4 %mem2%; Add4 %mem5%; Mult4 -1; Event Go; ENDON'],
                        ['Rule2', '+ ON Event#sC DO Backlog Var2 sC2; Event sC2; ENDON'],
                        ['Rule2', '+ ON Event#sC2 DO Backlog Var2 sC2; TuyaSend4 101,1; ENDON'],
                        ['Rule2', '+ ON TuyaReceived#Data=55AA00070005650400010075 DO Var2 sC3; ENDON'],
                        ['Rule2', '+ ON Event#C107 DO Var2 sC; ENDON ON Event#C108 DO Var2 sC; ENDON ON Event#C109 DO Var2 sC; ENDON ON Event#C110 DO Var2 sC; END ON ON Event#C111 DO Var2 sC; ENDON'],
                        ['Rule2', '1'],
                        ['Rule3', 'ON Event#C100 DO Var2 sC; ENDON ON Event#C101 DO Var2 sC; ENDON ON Event#C102 DO Var2 sC; ENDON ON Event#C103 DO Var2 sC; ENDON ON Event#C104 DO Var2 sC; ENDON ON Event#C105 DO Var2 sC; ENDON ON Event#C106 DO Var2 sC; ENDON ON Event#O0 DO Var2 sC; ENDON ON Event#O-1 DO Var2 sC; ENDON ON Event#O-2 DO Var2 sC; ENDON ON Event#O-3 DO Var2 sC; ENDON ON Event#O-4 DO Var2 sC; ENDON ON Event#O-5 DO Var2 sC; ENDON ON Event#O-6 DO Var2 sC; ENDON ON Event#O-12 DO Var2 sC; ENDON'],
                        ['Rule3', '1']],
        deviceLink: '',
        open: ["TuyaSend4", "101,0"],
        stop: ["TuyaSend4", "101,1"],
        close: ["TuyaSend4", "101,2"],],
        
        // https://tasmota.github.io/docs/#/devices/Sonoff-RF-Bridge-433pi 
        [typeId: 'sonoff-rf-bridge-parent' , 
        notForUniversal: true,
        comment: 'Functional - Need feedback',
        name: '',
        template: '',
        installCommands: [],
        deviceLink: 'https://templates.blakadder.com/sonoff_RF_bridge.html'],
        
        [typeId: 'rflink-parent' , 
        notForUniversal: true,
        comment: 'Functional - Need feedback',
        name: '',
        template: '',
        installCommands: [],
        deviceLink: 'http://www.rflink.nl/blog2/wiring'],
        
        // Generic Tasmota Devices:
        [typeId: '01generic-device',
        comment: 'Works with most devices' ,
        name: 'Generic Device',
        template: '',
        installCommands: [],
        deviceLink: ''],

        [typeId: '01generic-switch-plug',
        comment: 'Works as Plug/Outlet with Alexa' ,
        name: 'Generic Switch/Plug',
        template: '',
        installCommands: [],
        deviceLink: ''],

        [typeId: '01generic-switch-light',
         comment: 'Works as Light with Alexa' ,
        name: 'Generic Switch/Light',
        template: '',
        installCommands: [],
        deviceLink: ''],

        [typeId: '01generic-rgb-rgbw-controller-bulb-dimmer', 
        comment: 'RGB+WW+CW should all work properly',
        name: 'Generic RGB/RGBW Controller/Bulb/Dimmer',
        template: '',
        installCommands: [["WebLog", "2"]],
        deviceLink: ''],

        [typeId: '01generic-thp-device' ,
        name: 'Generic Temperature/Humidity/Pressure Device',
        template: '',
        installCommands: [["TempRes", (tempRes == '' || tempRes == null ? "1" : tempRes)]],
        deviceLink: ''],

        [typeId: '01generic-dimmer' ,
        name: 'Generic Dimmer',
        template: '',
        installCommands: [["WebLog", "2"]],
        deviceLink: ''],
    ]

    TreeMap deviceConfigurationsMap = [:] as TreeMap
    deviceConfigurations.each{
        deviceConfigurationsMap[it["typeId"]] = it
    }
    return deviceConfigurationsMap
}

def getDeviceConfiguration(String typeId) {
    TreeMap deviceConfigurationsMap = getDeviceConfigurations()
    try{
        return deviceConfigurationsMap[typeId]
    } catch(e) {
        log.warn "Failed to retrieve Device Configuration '$typeId': $e"
        return null
    }
}

def getDeviceConfigurationsAsListOption() {
    TreeMap deviceConfigurationsMap = getDeviceConfigurations()
    def items = []
    deviceConfigurationsMap.each { k, v ->
        def label = v["name"]
        if(v.containsKey("comment") && v["comment"].length() > 0) {
            label += " (${v["comment"]})"
        }
        if(!(v.containsKey("notForUniversal") && v["notForUniversal"] == true)) {
            items << ["${v["typeId"]}":"$label"] 
        }
    }
    return items
}

/**
 * --END-- DEVICE CONFIGURATIONS METHODS (helpers-device-configurations)
 */

Long getMillisSinceDate(myDate) {
    
    //myDate
    return now() - myDate.getTime()
}

String getTimeStringSinceMillis(millis) {
    Integer seconds = (Integer) (millis / 1000) % 60
    Integer minutes = (Integer) (millis / (1000*60)) % 60
    Integer hours = (Integer) (millis / (1000*60*60)) % 24
    Integer days = (Integer) (millis / (1000*60*60*24))
    return String.format("%dT%02d:%02d:%02d", days, hours, minutes, seconds)
}

String getTimeStringSinceDate(myDate) {
    return getTimeStringSinceMillis(getMillisSinceDate(myDate))
}

Map getTimeStringSinceDateWithMaximum(myDate, maxMillis) {
    def millis = getMillisSinceDate(myDate)
    return [time:getTimeStringSinceMillis(millis), red:millis > maxMillis]
}

void makeAppTitle() {
    section(getElementStyle('title', getMaterialIcon('build', 'icon-large') + "${app.label}" + getCSSStyles())){
        }
}

Map mainPage() {
	return dynamicPage(name: "mainPage", nextPage: null, uninstall: true, install: true) {
        makeAppTitle() // Also contains the CSS
        logging("Building mainPage", 1)
        // Hubitat green: #81BC00
        // box-shadow: 2px 3px #A9A9A9
        installCheck()
        initializeAdditional()
        if (state.appInstalled == 'COMPLETE') {
            section(getElementStyle('header', getMaterialIcon('settings_applications') + "Configure App"), hideable: true, hidden: false){
                getElementStyle('separator')
                //input(name: "sendToAWSwitch", type: "bool", defaultValue: "false", title: "Use App Watchdog to track this apps version info?", description: "Update App Watchdog", submitOnChange: "true")}
                generate_preferences(configuration_model_debug())
            
            //input(name: "pushAll", type: "bool", defaultValue: "false", submitOnChange: true, title: "Only send Push if there is something to actually report", description: "Push All")
            //href "deviceDiscoveryCancel", title:"Cancel Discover Device", description:""
            }
            section(getElementStyle('header', getMaterialIcon('library_add') + "Install New Devices"), hideable: true, hidden: false){
                href("deviceDiscovery", title:getMaterialIcon('', 'he-discovery_1') + "Discover Devices", description:"")
                href("manuallyAdd", title:getMaterialIcon('', 'he-add_1') + "Manually Add Device", description:"")
            }
            section(getElementStyle('header', getMaterialIcon('playlist_add') + 'Grant Access to Additional Devices'), hideable: true, hidden: true){
                paragraph("Select the devices to grant access to, if the device doesn't use a compatible driver it will be ignored, so selecting too many or the wrong ones, doesn't matter. Easiest is probably to just select all devices. Only Parent devices are shown.")
                input(name:	"devicesSelected", type: "capability.refresh", title: "Available Devices", multiple: true, required: false, submitOnChange: true)
            }
            section(getElementStyle('header', getMaterialIcon('', 'he-settings1') + "Configure Devices"), hideable: true, hidden: false){ 
                paragraph('<div style="margin: 8px;">All devices below use a compatible driver, if any device is missing, add them above in "Grant Access to Additional Devices". Newly selected devices will not be shown until after you\'ve pressed Done.</div>')
                
                //input(name: "refreshDevices", type: "bool", defaultValue: "false", submitOnChange: true, title: "Refresh Devices", description: "Refresh Devices Desc")
                href("resultPage", title:getMaterialIcon('autorenew') + "Result Page", description: "")
                href("refreshDevices", title:getMaterialIcon('autorenew') + "Refresh Devices", description: "")

                state.devices.each { rawDev ->
                    def cDev = getTasmotaDevice(rawDev.deviceNetworkId)
                    //getLastActivity()
                    if(cDev != null) {
                        href("configureTasmotaDevice", title:"${getMaterialIcon('', 'he-bulb_1 icon-small')} $cDev.label", description:"", params: [did: cDev.deviceNetworkId])
                        
                        Map lastActivity = getTimeStringSinceDateWithMaximum(cDev.getLastActivity(), 2*60*60*1000)
                        // Status
                        def deviceStatus = cDev.currentState('presence')?.value
                        logging("$cDev.id - deviceStatus = $deviceStatus", 1)
                        if(deviceStatus == null || deviceStatus == "not present") {
                            deviceStatus = "Timeout"
                        } else {
                            deviceStatus = "Available"
                        }

                        // Wifi
                        def wifiSignalQuality = cDev.currentState('wifiSignal')
                        
                        boolean wifiSignalQualityRed = true
                        if(wifiSignalQuality != null) {
                            wifiSignalQuality = wifiSignalQuality.value
                            wifiSignalQualityRed = extractInt(wifiSignalQuality) < 50
                        }
                        logging("$cDev.id - wifiSignalQuality = $wifiSignalQuality", 1)
                        String uptime = "${cDev.getDeviceDataByName('uptime')}"
                        String firmware = "${cDev.getDeviceDataByName('firmware')}"
                        String driverVersion = "${cDev.getDeviceDataByName('driver')}"
                        String driverName = "${getDeviceDriverName(cDev)}"
                        getDeviceTable([[href:getDeviceConfigLink(cDev.id)],
                                        [data:rawDev['data']['ip']],
                                        //[data:runDeviceCommand(getTasmotaDevice(cDev.deviceNetworkId), 'getDeviceDataByName', ['uptime'])],])
                                        [data:uptime, red:uptime == "null"],
                                        [data:lastActivity['time'], red:lastActivity['red']],
                                        [data:"${wifiSignalQuality}", red:wifiSignalQualityRed],
                                        [data:firmware, red:firmware == "null"],
                                        [data:driverVersion, red:driverVersion == "null"],
                                        [data:deviceStatus, red:deviceStatus != "Available"],
                                        [data:driverName, red:driverName == "null"],])
                                // it.label
                            //btnParagraph([[href:getDeviceConfigLink(cDev.id), target:"_blank", title:"Config"],
                            //            [href:getDeviceTasmotaConfigLink(cDev['data']['ip']), target:"_blank", title:'Tasmota&nbsp;Web&nbsp;Config (' + cDev['data']['ip'] + ')']],
                            //)
                                //paragraph('<table style="border-spacing: 10px 0px"><tr><td class="btn btn-default btn-lg hrefElem mdl-button--raised mdl-shadow--2dp"><a style="color: #000;" href="' + "${getDeviceConfigLink(it.id)}" + '" target="deviceConfig">Config</a></td>' + 
                                        //' | <a href="' + "${getDeviceLogLink(it.id)}" + '" target="deviceConfig">Log</a>' +
                                //        '<td class="btn btn-default btn-lg hrefElem mdl-button--raised mdl-shadow--2dp"><a style="color: #000;" href="' + "${getDeviceTasmotaConfigLink(it['data']['ip'])}" + '" target="deviceWebConfig">Tasmota&nbsp;Web&nbsp;Config (' + it['data']['ip'] + ')</a></td></tr></table>' )
                        
                        //%7B%22did%22%3A%225002915AFD0A%22%7D
                        //%7B%22did%22%3A%222CF43222E6AD%22%7D
                    }

                }
            }
            section(getElementStyle('header', "More things"), hideable: true, hidden: true){
                paragraph("Select the devices to configure, if the device doesn't use a compatible driver it will be ignored, so selecting too many or the wrong ones, doesn't matter. Easiest is probably to just select all devices. Only Parent devices are shown.")
                
                input(name:	"devicesAvailable", type: "enum", title: "Available Devices", multiple: true, required: false, submitOnChange: true, options: state.devicesSelectable)
            }
        } else {
            section(getElementStyle('subtitle', "Configure")){
                generate_preferences(configuration_model_debug())
            }
        }
        footer()
    }
}

def refreshDevices(){
    logging("refreshDevices()", 1)
    Integer numDevices = 0
    Integer numDevicesSuccess = 0
    getAllTasmotaDevices().each {
        numDevices += 1
        try{
            //logging("BEFORE Refreshing Device \"${it.label}\" (${it.id})", 1)
            it.refresh()
            logging("AFTER Refreshing Device \"${it.label}\" (${it.id})", 1)
            numDevicesSuccess += 1
        } catch(e) {
            log.warn("Failed to Refresh Device \"${it.label}\" (${it.id})")
        }
    }
    String result = "COMPLETE REFRESH FAILURE!"
    if(numDevicesSuccess == numDevices) {
        result = "All $numDevices Device(s) have been refreshed!"
    } else {
        result = "PARTIAL FAILURE: $numDevicesSuccess of $numDevices Device(s) have been refreshed! (${numDevices - numDevicesSuccess} failed!)"
    }
    updatedAdditional()
    return resultPage("refreshDevices", "Devices Refreshed", result)
}

Map resultPage(){
    logging("resultPage()", 1)
    return resultPage("resultPage", "Result Page", "My little result...")
}

Map resultPage(name, title, result, nextPage = "mainPage"){
    logging("resultPage(name = $name, title = $title, result = $result, nextPage = $nextPage)", 1)

    return dynamicPage(name: name, title: "", nextPage: nextPage) {
        makeAppTitle() // Also contains the CSS

        section(getElementStyle('header', getMaterialIcon('done') + "Action Completed"), hideable: true, hidden: false){
            paragraph("<div style=\"font-size: 16px;\">${result}</div>")
        }
    }
}

def getElementStyle(style, content=""){
    switch (style) {
        case 'header':
            //return '<div style="font-weight: bold; color:#fff;">' + content + '</div>'
            return content
            break
        case 'title':
            return '<h2 style="font-weight: bold; color:#382e2b;">' + content + '</h2>'
            break
        case 'subtitle':
            return '<div style="font-weight: bold; color:#382e2b;">' + content + '</div>'
            break
        case 'line':
            return '<hr style="height: 1px; border: 0px; background-color:#382e2b;"></hr>'
        case 'separator':
            return '\n<hr style="background-color:#1A77C9; height: 1px; border: 0;"></hr>'
            break
    }
}

String getMaterialIcon(iconName, extraClass='') {
    // Browse icons here
    // https://material.io/resources/icons/?style=baseline
    // known HE icons (set as class): he-bulb_1, he-settings1, he-file1, he-default_dashboard_icon, he-calendar1
    // he-discovery_1, he-add_2, he-door_closed
    return '<i class="material-icons icon-position ' + extraClass + '">' + iconName + '</i>'
}

//#.form div.mdl-cell h4.pre {
String getCSSStyles() {
    return '''<style>
/* General App Styles */
.btn {
    font-family: "Roboto","Helvetica","Arial",sans-serif;
}
.mdl-card, .mdl-switch__label, .mdl-textfield  {
    font-size: 14px;
    font-family: "Roboto","Helvetica","Arial",sans-serif;
}
.btn-sub {
    padding: 2px 30px 2px 2px;
}
div.mdl-button--raised {
    font-weight: bold; 
    color:#fff; 
    background-color:#81bc00; 
    border: 1px solid;
}
div.mdl-button--raised:hover, div.mdl-button--raised:focus {
    color: #212121;
    background-color:#91d844; 
}
.btn-sub.hrefElem:before {
    top: calc(50% - 0.75625rem);
}
div.mdl-button--raised h4.pre {
    font-weight: bold; 
    color: #fff;
    vertical-align: middle;
}

/* Icon Styles */
.icon-position {
    margin-right: 12px;
    vertical-align: middle;
}
.icon-tiny {
    margin-right: 8px;
    font-size: 14px;
}
.icon-small {
    margin-right: 8px;
    font-size: 18px;
}
.icon-large {
    margin-right: 12px;
    font-size: 32px;
}

/* Configure Devices List Styles */
#collapse4 .hrefElem::before {
    filter: invert(100%);
}
#collapse4 .hrefElem:hover::before, #collapse4 .hrefElem:focus::before {
    filter: invert(0%);
}
#collapse4 table .hrefElem::before {
    filter: invert(0%);
}
#collapse4 .btn-block {
    color: #f5f5f5;
    background-color: #382e2b;
    
    font-size: 14px;
    /*font-size: calc(100% + 0.08vw);*/
    max-width: inherit;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}
#collapse4 .btn-block span {
    white-space: nowrap !important;
    max-width: inherit;
}
#collapse4 .btn-block:hover, #collapse4 .btn-block:focus {
    color: #212121;
    background-color: #e0e0e0;
}
#collapse4 div.mdl-textfield {
    margin: 0px;
}
.device-config_table {
    border-spacing: 2px 0px;
    table-layout:fixed;
    width: 100%
}
.device-config_td {
    text-align: center;
    vertical-align: middle;
}
.device-config_btn {
    width: 100%;
}
.device-config_table th, .device-config_table td {
    font-family: "Roboto","Helvetica","Arial",sans-serif;
    font-size: 13px;
    vertical-align: middle;
    width: 100%;
}
.device-config_table th div, .device-config_td div, .device-config_table td a {
    text-align: center;
    white-space: nowrap !important;
    max-width: inherit;
    overflow: hidden;
    text-overflow: ellipsis;
    width: 100%;
    display: block;
}
.device-config_btn_icon {
    text-align: center;
    width: 100%;
}

/* Action Buttons */
#collapse4 [name*="refreshDevices"] {
    float: right !important;
}
#collapse4 [name*="resultPage"] {
    float: left !important;
}
#collapse4 [name*="refreshDevices"], #collapse4 [name*="resultPage"] {
    color: #000;
    width: 170px !important;
    min-width: 170px;
    background: rgba(158,158,158,.2);
    border: none;
    margin-left: 0px;
    text-align: center !important;
    vertical-align: middle;
    line-height: 36px;
    padding-right: unset;
    padding: 0px 16px;
    display:inline;
}
#collapse4 .mdl-cell--12-col:nth-of-type(2), #collapse4 .mdl-cell--12-col:nth-of-type(3) {
    width: 50% !important;
    display:inline !important;
}
#collapse4 [name*="refreshDevices"] span, #collapse4 [name*="resultPage"] span {
    font-weight: 500;
    text-align: center !important;
    white-space: nowrap !important;
}
#collapse4 [name*="refreshDevices"]::before, #collapse4 [name*="resultPage"]::before {
    content: "";
}

@media (min-width: 840px)
.mdl-cell--8-col, .mdl-cell--8-col-desktop.mdl-cell--8-col-desktop {
    width: calc(76.6666666667% - 16px);
}
</style>'''
}

Map btnParagraph(buttons, extra="") {
    //getDeviceConfigLink(it.id)
    String content = '<table style="border-spacing: 10px 0px"><tr>'
    buttons.each {
        //content += '<td class="btn btn-default btn-lg hrefElem mdl-button--raised mdl-shadow--2dp">'
        content += '<td>'
        
        content += '<a style="color: #000;" href="' + "${it['href']}" + '" target="' +"${it['target']}" + '">'
        
        content += '<button type="button" class="btn btn-default hrefElem btn-lg mdl-button--raised mdl-shadow--2dp btn-sub">'
        
        content += "${it['title']}"
        //content += '<i class="material-icons icon-position hrefElemAlt">arrow_drop_down</i>'
        content += '</button></a></td>'

//                                '<td class="btn btn-default btn-lg hrefElem mdl-button--raised mdl-shadow--2dp"><a style="color: #000;" href="' + "${getDeviceTasmotaConfigLink(it['data']['ip'])}" + '" target="deviceWebConfig">Tasmota&nbsp;Web&nbsp;Config (' + it['data']['ip'] + ')</a></td> )
    }
    content += '</tr></table>' // + extra
    return paragraph(content) 
}

def getDeviceTableCell(deviceInfoEntry, link=true) {
    it = deviceInfoEntry
    def content = '<td class="device-config_td ' + "${it['td_class']}" + '">'
        
    if(link == true) {
        content += '<a class="device-config_btn ' + "${it['class']}" + '" href="' + "${it['href']}" + '" target="' +"${it['target']}" + '">'
    }
    
    //content += '<button type="button" class="btn btn-default hrefElem btn-lg mdl-button--raised mdl-shadow--2dp btn-sub">'
    extraTitle = ""
    if(it['title'] != null && it['title'].indexOf('material-icons') == -1) {
        extraTitle = "title=\"${it['title']}\""
    }
    if(it['red'] == true) {
        
            content += "<div ${extraTitle} style=\"color: red;\" >${it['title']}</div>"
        
    } else {
        content += "<div ${extraTitle} >${it['title']}</div>"
    }
    //content += '<i class="material-icons icon-position hrefElemAlt">arrow_drop_down</i>'
    //content += '</button></a></td>'
    if(link == true) {
        content += '</a>'
    }
    content += '</td>'

    return content
}

def getDeviceTable(deviceInfo, extra="") {
    //getDeviceConfigLink(it.id)
    def content = '<table class="device-config_table"><tr>'
    content += '<th style="width: 40px;"><div>Config</div></th>'
    content += '<th style="width: 100px;"><div>Tasmota&nbsp;Config</div></th>'
    content += '<th style="width: 80px;"><div>Uptime</div></th>'
    content += '<th style="width: 80px;"><div>Heartbeat</div></th>'
    content += '<th style="width: 33px;"><div>Wifi</div></th>'
    content += '<th style="width: 100px;"><div>Firmware</div></th>'
    content += '<th style="width: 60px;"><div>Driver</div></th>'
    content += '<th style="width: 60px;"><div>Status</div></th>'
    content += '<th style=""><div>Type</div></th>'
    content += '</tr><tr>'

    // Config Link
    content += getDeviceTableCell([href:deviceInfo[0]['href'], 
        target:'_blank', title:getMaterialIcon('', 'he-settings1 icon-tiny device-config_btn_icon')])

    // Tasmota Web Config Link
    content += getDeviceTableCell([class:'device-config_btn', href:getDeviceTasmotaConfigLink(deviceInfo[1]['data']), 
        target:'_blank', title:deviceInfo[1]['data']])

    // Tasmota Device Uptime
    content += getDeviceTableCell([title:deviceInfo[2]['data'], red:deviceInfo[2]['red']], false)

    // Tasmota Heartbeat
    content += getDeviceTableCell([title:deviceInfo[3]['data'], red:deviceInfo[3]['red']], false)

    // Wifi Signal Quality
    content += getDeviceTableCell([title:deviceInfo[4]['data'], red:deviceInfo[4]['red']], false)

    // Firmware Version
    content += getDeviceTableCell([title:deviceInfo[5]['data'], red:deviceInfo[5]['red']], false)

    // Driver Version
    content += getDeviceTableCell([title:deviceInfo[6]['data'], red:deviceInfo[6]['red']], false)

    // Status
    content += getDeviceTableCell([title:deviceInfo[7]['data'], red:deviceInfo[7]['red']], false)

    // Driver Type
    content += getDeviceTableCell([title:deviceInfo[8]['data'], red:deviceInfo[8]['red']], false)

    content += '</tr>'
    content += '<tr>'

    content += '</tr></table>' // + extra
    paragraph(content) 
}

def configureTasmotaDevice(params) {
    if (params?.did || params?.params?.did) {
        if (params.did) {
            //getTasmotaDevice
            //
            state.currentDeviceId = params.did
            state.currentDisplayName = getTasmotaDevice(params.did).label
            logging("params.did: $params.did, label: ${getTasmotaDevice(params.did)?.label}", 1)
        } else {
            logging("params.params.did: $params.params.did", 1)
            state.currentDeviceId = params.params.did
            state.currentDisplayName = getTasmotaDevice(params.params.did)?.label
        }
    }
    device = getTasmotaDevice(state.currentDeviceId)
    state.currentDisplayName = device.label
    logging("state.currentDeviceId: ${state.currentDeviceId}, label: ${device.label}", 1)
    //if (device != null) device.configure()
    dynamicPage(name: "configureTasmotaDevice", title: "Configure Tasmota-based Devices created with this app", nextPage: null) {
            section {
                app.updateSetting("${state.currentDeviceId}_label", device.label)
                input "${state.currentDeviceId}_label", "text", title:"Device Name", description: "", required: false
                href "changeName", title:"Change Device Name", description: "Edit the name above and click here to change it"
            }
            section {
                href "deleteDevice", title:"Delete $state.label", description: ""
            }
    }
    // device = getChildDevice(did)
    //dynamicPage(name: "configureTasmotaDevice", nextPage: null, uninstall: false, install: false) {
    //    paragraph('Device: <a href="' + "${getDeviceConfigLink(device.deviceId)}" + '" target="deviceConfig">' + device.label + '</a>' + 
    //                          ' - <a href="' + "${getDeviceLogLink(device.deviceId)}" + '" target="deviceConfig">Device&nbsp;Log</a>')
    //}
}

def manuallyAdd(){
   dynamicPage(name: "manuallyAdd", title: "Manually add a Tasmota-based Device", nextPage: "manuallyAddConfirm") {
		section {
			paragraph "This process will manually create a Tasmota-based Device with the entered IP address. Tasmota Connect then communicates with the device to obtain additional information from it. Make sure the device is on and connected to your wifi network."
            input "deviceType", "enum", title:"Device Type", description: "", required: true, options: 
                // BEGIN:makeTasmotaConnectDriverListV1()
                ["Tasmota - Universal Parent Testing",
                ]
                // END:  makeTasmotaConnectDriverListV1()
            input "ipAddress", "text", title:"IP Address", description: "", required: true 
		}
    }
}

def manuallyAddConfirm(){
   if ( ipAddress =~ /^(?:[0-9]{1,3}\.){3}[0-9]{1,3}$/) {
       logging("Creating Tasmota-based Wifi Device with dni: ${convertIPtoHex(ipAddress)}", 1)
       addChildDevice("tasmota", deviceType ? deviceType : "Tasmota - Generic Wifi Switch/Plug", "${convertIPtoHex(ipAddress)}", location.hubs[0].id, [
           "label": (deviceType ? deviceType : "Tasmota - Generic Wifi Switch/Plug") + " (${ipAddress})",
           "data": [
           "ip": ipAddress,
           "port": "80" 
           ]
       ])
   
       app.updateSetting("ipAddress", "")
            
       dynamicPage(name: "manuallyAddConfirm", title: "Manually add a Tasmota-based Device", nextPage: "mainPage") {
		   section {
			   paragraph "The device has been added. Press next to return to the main page."
	    	}
       }
    } else {
        dynamicPage(name: "manuallyAddConfirm", title: "Manually add a Tasmota-based Device", nextPage: "mainPage") {
		    section {
			    paragraph "The entered ip address is not valid. Please try again."
		    }
        }
    }
}

def deleteDevice(){
    try {
        unsubscribe()
        deleteChildDevice(state.currentDeviceId)
        dynamicPage(name: "deleteDevice", title: "Deletion Summary", nextPage: "mainPage") {
            section {
                paragraph "The device has been deleted. Press next to continue"
            } 
        }
    
	} catch (e) {
        dynamicPage(name: "deleteDevice", title: "Deletion Summary", nextPage: "mainPage") {
            section {
                paragraph "Error: ${(e as String).split(":")[1]}."
            } 
        }
    
    }
}

def getDeviceDriverName(device) {
    //getTasmotaDevice device.deviceNetworkId
    driverName = 'Unknown'
    try {
        driverName = runDeviceCommand(device, 'getDeviceInfoByName', args=['name'])
    } catch(e) {
        logging("Failed getting DriverName ($e), trying again...", 1)
        device = getTasmotaDevice(device.deviceNetworkId)
        try{
            driverName = runDeviceCommand(device, 'getDeviceInfoByName', args=['name'])
        } catch(e1) {
            driverName = "Unknown"
        }
    }
    if (driverName.startsWith("Tasmota - ")) driverName = driverName.substring(10)
    logging("Found Driver Name: '$driverName'", 0)
    return driverName
}

def getDeviceConfigLink(deviceId) {
    ///device/edit/
    return("http://${location.hub.localIP}/device/edit/${deviceId}")
}

def getDeviceLogLink(deviceId) {
    ///device/edit/
    return("http://${location.hub.localIP}/logs#dev${deviceId}")
}

def getDeviceTasmotaConfigLink(deviceIP) {
    ///device/edit/
    return("http://${deviceIP}/")
}

def installCheck() {
	state.appInstalled = app.getInstallationState()
	
	if (state.appInstalled != 'COMPLETE') {
		section{paragraph "Please hit 'Done' to finish installing '${app.label}'"}
  	}
  	else {
    	logging("Parent Installed OK", 1)
  	}
}

def footer() {
    section() {
        paragraph(getElementStyle('line'))
        paragraph('<div style="color:#382e2b; text-align:center">' + app.label + ' - Copyright&nbsp;2020&nbsp;Markus&nbsp;Liljergren - <a href="https://github.com/markus-li/Hubitat/tree/release" target="_blank">GitHub repo</a></div>')
    }
}

/*
	installedAdditional

	Purpose: initialize the app
	Note: if present, called from installed() in all drivers/apps
    installed() does NOT call initalize() by default, so if needed, call it here.
*/
def installedAdditional() {
    logging("installedAdditional()", 1)
	initialize()
}

def uninstalled() {
    logging("uninstalled()", 1)
    unsubscribe()
    unschedule()
}

def updatedAdditional() {
    logging("updatedAdditional()", 1)
	unsubscribe()
    unschedule()
    devices = getAllTasmotaDevices()
    
    //app.removeSetting("devicesAvailable")
    //app.updateSetting("devicesAvailable", devices)
    //devicesSelected = devices
    state.devices = devices.sort({ a, b -> a["label"] <=> b["label"] })
    def devicesSelectable = []
    state.devices.each { devicesSelectable << ["${it.deviceNetworkId}":"${it.label}"] }

    logging("devicesSelectable: ${devicesSelectable}", 1)
    state.devicesSelectable = devicesSelectable
	initialize()
}

def runDeviceCommand(device, cmd, args=[]) {
    def jsonSlurper = new JsonSlurper()
    logging("runDeviceCommand(device=${device.deviceId.toString()}, cmd=${cmd}, args=${args})", 0)
    // Since Refresh is defined as a command in all my drivers, 
    // it can be used to forward the call to deviceCommand.
    //device.parse(JsonOutput.toJson([cmd: cmd, args: args]), 1)
    
    device.refresh(JsonOutput.toJson([cmd: cmd, args: args]))
    //device.deviceCommand(JsonOutput.toJson([cmd: cmd, args: args]))
    r = null
    r = jsonSlurper.parseText(device.getDataValue('appReturn'))

    device.updateDataValue('appReturn', null)
    return r
}

// 
def getAllTasmotaDevices() {
    toRemove = []
    devicesFiltered = []
    devicesSelected.eachWithIndex { it, i ->
        namespace = 'unknown'
        try {
            //runDeviceCommand(it, 'setDeviceInfoAsData', ['namespace'])
            //namespace = runDeviceCommand(it, 'getDeviceInfoByName', ['namespace'])
            //runDeviceCommand(it, 'testing', [])
            namespace = it.getDataValue('namespace')
        } catch(e) {
            // This only fails when a device is of no interest to us anyway, so mute it...
            logging("Device ID: ${it.deviceId.toString()}, e: ${e}", 1)
        }

        logging("Device ID: ${it.deviceId.toString()}, Parent ID: ${it.parentAppId.toString()}, name: ${it.getName()}, namespace: ${namespace}, deviceNetworkId: ${it.deviceNetworkId}, i: ${i}", 0)
        //logging(it.getProperties().toString(), 1)
        //logging(it.parentAppId, 1)
        //parentAppId
        if(namespace == 'tasmota' && it.parentAppId != app.id) {
            devicesFiltered << it
        }
    }
    childDevices = getChildDevices()
    logging("getChildDevices: ${getChildDevices()}", 1)
    childDevices.eachWithIndex { it, i ->
        namespace = 'unknown'
        try {
            //runDeviceCommand(it, 'setDeviceInfoAsData', ['namespace'])
            namespace = runDeviceCommand(it, 'getDeviceInfoByName', ['namespace'])
            //runDeviceCommand(it, 'testing', [])
            //namespace = it.getDataValue('namespace')
        } catch(e) {
            // This only fails when a device is of no interest to us anyway, so mute it...
            logging("Device ID: ${it.id.toString()}, e: ${e}", 1)
        }

        logging("Device ID: ${it.id.toString()}, Parent ID: ${it.parentAppId.toString()}, name: ${it.getName()}, namespace: ${namespace}, deviceNetworkId: ${it.deviceNetworkId}, i: ${i}", 0)
        //logging(it.getProperties().toString(), 1)
        //logging(it.parentAppId, 1)
        //parentAppId
        if(namespace == 'tasmota') {
            devicesFiltered << it
        }
    }
    //devicesFiltered << childDevices
    return devicesFiltered
}

def getTasmotaDevice(deviceNetworkId) {
    r = getChildDevice(deviceNetworkId)
    if(r == null) {
        devicesSelected.each {
            //logging("'" + it.deviceNetworkId + "' =? '" + deviceNetworkId + "'", 1)
            if(it.deviceNetworkId == deviceNetworkId) {
                //logging("'" + it.deviceNetworkId + "' == '" + deviceNetworkId + "' (dev: ${it})", 1)
                r = it
            }
        }
    }
    return r
}

/*
	initializeAdditional

	Purpose: initialize the app
	Note: if present, called from initialize() in all drivers/apps
    Called when Done is pressed in the App
*/
def initializeAdditional() {
    logging("initializeAdditional()", 1)
    //getAllTasmotaDevices()
    //ssdpSubscribe()
    //runEvery5Minutes("ssdpDiscover")
}

// BEGIN:getLoggingFunction()
/* Logging function included in all drivers */
private def logging(message, level) {
    if (infoLogging == true) {
        logLevel = 100
    }
    if (debugLogging == true) {
        logLevel = 1
    }
    if (logLevel != "0"){
        switch (logLevel) {
        case "-1": // Insanely verbose
            if (level >= 0 && level < 100)
                log.debug "$message"
            else if (level == 100)
                log.info "$message"
        break
        case "1": // Very verbose
            if (level >= 1 && level < 99)
                log.debug "$message"
            else if (level == 100)
                log.info "$message"
        break
        case "10": // A little less
            if (level >= 10 && level < 99)
                log.debug "$message"
            else if (level == 100)
                log.info "$message"
        break
        case "50": // Rather chatty
            if (level >= 50 )
                log.debug "$message"
        break
        case "99": // Only parsing reports
            if (level >= 99 )
                log.debug "$message"
        break
        }
    }
}
// END:  getLoggingFunction()


/**
 * ALL DEBUG METHODS (helpers-all-debug)
 *
 * Helper Debug functions included in all drivers/apps
 */
String configuration_model_debug() {
    if(!isDeveloperHub()) {
        if(!isDriver()) {
            app.removeSetting("logLevel")
            app.updateSetting("logLevel", "0")
        }
        return '''
<configuration>
<Value type="bool" index="debugLogging" label="Enable debug logging" description="" value="true" submitOnChange="true" setting_type="preference" fw="">
<Help></Help>
</Value>
<Value type="bool" index="infoLogging" label="Enable descriptionText logging" description="" value="true" submitOnChange="true" setting_type="preference" fw="">
<Help></Help>
</Value>
</configuration>
'''
    } else {
        if(!isDriver()) {
            app.removeSetting("debugLogging")
            app.updateSetting("debugLogging", "false")
            app.removeSetting("infoLogging")
            app.updateSetting("infoLogging", "false")
        }
        return '''
<configuration>
<Value type="list" index="logLevel" label="Debug Log Level" description="Under normal operations, set this to None. Only needed for debugging. Auto-disabled after 30 minutes." value="-1" submitOnChange="true" setting_type="preference" fw="">
<Help>
</Help>
    <Item label="None" value="0" />
    <Item label="Insanely Verbose" value="-1" />
    <Item label="Very Verbose" value="1" />
    <Item label="Verbose" value="10" />
    <Item label="Reports+Status" value="50" />
    <Item label="Reports" value="99" />
    // BEGIN:getSpecialDebugEntry()
    // END:  getSpecialDebugEntry()
</Value>
</configuration>
'''
    }
}

/**
 *   --END-- ALL DEBUG METHODS (helpers-all-debug)
 */
/**
 * ALL DEFAULT METHODS (helpers-all-default)
 *
 * Helper functions included in all drivers/apps
 */

boolean isDriver() {
    try {
        // If this fails, this is not a driver...
        getDeviceDataByName('_unimportant')
        logging("This IS a driver!", 0)
        return true
    } catch (MissingMethodException e) {
        logging("This is NOT a driver!", 0)
        return false
    }
}

void deviceCommand(cmd) {
    def jsonSlurper = new JsonSlurper()
    cmd = jsonSlurper.parseText(cmd)
    logging("deviceCommand: ${cmd}", 0)
    r = this."${cmd['cmd']}"(*cmd['args'])
    logging("deviceCommand return: ${r}", 0)
    updateDataValue('appReturn', JsonOutput.toJson(r))
}

/*
	initialize

	Purpose: initialize the driver/app
	Note: also called from updated()
*/
// Call order: installed() -> configure() -> updated() -> initialize()
void initialize() {
    logging("initialize()", 100)
	unschedule()
    // disable debug logs after 30 min, unless override is in place
	if (logLevel != "0" && logLevel != "100") {
        if(runReset != "DEBUG") {
            log.warn "Debug logging will be disabled in 30 minutes..."
        } else {
            log.warn "Debug logging will NOT BE AUTOMATICALLY DISABLED!"
        }
        runIn(1800, "logsOff")
    }
    if(isDriver()) {
        if(!isDeveloperHub()) {
            device.removeSetting("logLevel")
            device.updateSetting("logLevel", "0")
        } else {
            device.removeSetting("debugLogging")
            device.updateSetting("debugLogging", "false")
            device.removeSetting("infoLogging")
            device.updateSetting("infoLogging", "false")
        }
    }
    try {
        // In case we have some more to run specific to this driver/app
        initializeAdditional()
    } catch (MissingMethodException e) {
        // ignore
    }
    refresh()
}

/**
 * Automatically disable debug logging after 30 mins.
 *
 * Note: scheduled in Initialize()
 */
void logsOff() {
    if(runReset != "DEBUG") {
        log.warn "Debug logging disabled..."
        // Setting logLevel to "0" doesn't seem to work, it disables logs, but does not update the UI...
        //device.updateSetting("logLevel",[value:"0",type:"string"])
        //app.updateSetting("logLevel",[value:"0",type:"list"])
        // Not sure which ones are needed, so doing all... This works!
        if(isDriver()) {
            device.clearSetting("logLevel")
            device.removeSetting("logLevel")
            device.updateSetting("logLevel", "0")
            state?.settings?.remove("logLevel")
            device.clearSetting("debugLogging")
            device.removeSetting("debugLogging")
            device.updateSetting("debugLogging", "false")
            state?.settings?.remove("debugLogging")
            
        } else {
            //app.clearSetting("logLevel")
            // To be able to update the setting, it has to be removed first, clear does NOT work, at least for Apps
            app.removeSetting("logLevel")
            app.updateSetting("logLevel", "0")
            app.removeSetting("debugLogging")
            app.updateSetting("debugLogging", "false")
        }
    } else {
        log.warn "OVERRIDE: Disabling Debug logging will not execute with 'DEBUG' set..."
        if (logLevel != "0" && logLevel != "100") runIn(1800, logsOff)
    }
}

Boolean isDeveloperHub() {
    return generateMD5(location.hub.zigbeeId as String) == "125fceabd0413141e34bb859cd15e067"
}

def getEnvironmentObject() {
    if(isDriver()) {
        return device
    } else {
        return app
    }
}

private def getFilteredDeviceDriverName() {
    def deviceDriverName = getDeviceInfoByName('name')
    if(deviceDriverName.toLowerCase().endsWith(' (parent)')) {
        deviceDriverName = deviceDriverName.substring(0, deviceDriverName.length()-9)
    }
    return deviceDriverName
}

private def getFilteredDeviceDisplayName() {
    def deviceDisplayName = device.displayName.replace(' (parent)', '').replace(' (Parent)', '')
    return deviceDisplayName
}

def generate_preferences(configuration_model) {
    def configuration = new XmlSlurper().parseText(configuration_model)
   
    configuration.Value.each {
        if(it.@hidden != "true" && it.@disabled != "true") {
            switch(it.@type) {   
                case "number":
                    input("${it.@index}", "number",
                        title:"${addTitleDiv(it.@label)}" + "${it.Help}",
                        description: makeTextItalic(it.@description),
                        range: "${it.@min}..${it.@max}",
                        defaultValue: "${it.@value}",
                        submitOnChange: it.@submitOnChange == "true",
                        displayDuringSetup: "${it.@displayDuringSetup}")
                    break
                case "list":
                    def items = []
                    it.Item.each { items << ["${it.@value}":"${it.@label}"] }
                    input("${it.@index}", "enum",
                        title:"${addTitleDiv(it.@label)}" + "${it.Help}",
                        description: makeTextItalic(it.@description),
                        defaultValue: "${it.@value}",
                        submitOnChange: it.@submitOnChange == "true",
                        displayDuringSetup: "${it.@displayDuringSetup}",
                        options: items)
                    break
                case "password":
                    input("${it.@index}", "password",
                            title:"${addTitleDiv(it.@label)}" + "${it.Help}",
                            description: makeTextItalic(it.@description),
                            submitOnChange: it.@submitOnChange == "true",
                            displayDuringSetup: "${it.@displayDuringSetup}")
                    break
                case "decimal":
                    input("${it.@index}", "decimal",
                            title:"${addTitleDiv(it.@label)}" + "${it.Help}",
                            description: makeTextItalic(it.@description),
                            range: "${it.@min}..${it.@max}",
                            defaultValue: "${it.@value}",
                            submitOnChange: it.@submitOnChange == "true",
                            displayDuringSetup: "${it.@displayDuringSetup}")
                    break
                case "bool":
                    input("${it.@index}", "bool",
                            title:"${addTitleDiv(it.@label)}" + "${it.Help}",
                            description: makeTextItalic(it.@description),
                            defaultValue: "${it.@value}",
                            submitOnChange: it.@submitOnChange == "true",
                            displayDuringSetup: "${it.@displayDuringSetup}")
                    break
            }
        }
    }
}

/*
    General Mathematical and Number Methods
*/
BigDecimal round2(BigDecimal number, Integer scale) {
    Integer pow = 10;
    for (Integer i = 1; i < scale; i++)
        pow *= 10;
    BigDecimal tmp = number * pow;
    return ( (Float) ( (Integer) ((tmp - (Integer) tmp) >= 0.5f ? tmp + 1 : tmp) ) ) / pow;
}

String generateMD5(String s) {
    return MessageDigest.getInstance("MD5").digest(s.bytes).encodeHex().toString()
}

Integer extractInt(String input) {
  return input.replaceAll("[^0-9]", "").toInteger()
}

/**
 * --END-- ALL DEFAULT METHODS (helpers-all-default)
 */
/**
 * APP DEFAULT METHODS (helpers-app-default)
 *
 * Methods used in all APPS
 */

def installed() {
	logging("installed()", 100)
    
	try {
        // In case we have some more to run specific to this App
        installedAdditional()
    } catch (MissingMethodException e) {
        // ignore
    }
}

/**
 * --END-- APP DEFAULT METHODS (helpers-app-default)
 */

/**
 * TASMOTA METHODS (helpers-tasmota)
 *
 * Helper functions included in all Tasmota drivers
 */

// Call order: installed() -> configure() -> updated() -> initialize() -> refresh()
void refresh() {
	logging("refresh()", 100)
    def metaConfig = null
    if(isDriver()) {
        // Clear all old state variables, but ONLY in a driver!
        state.clear()

        // Retrieve full status from Tasmota
        getAction(getCommandString("Status", "0"), callback="parseConfigureChildDevices")
        getDriverVersion()

        updateDataValue('namespace', getDeviceInfoByName('namespace'))

        //logging("this.binding.variables = ${this.binding.variables}", 1)
        //logging("settings = ${settings}", 1)
        //logging("getDefinitionData() = ${getDefinitionData()}", 1)
        //logging("getPreferences() = ${getPreferences()}", 1)
        //logging("getSupportedCommands() = ${device.getSupportedCommands()}", 1)
        //logging("Seeing these commands: ${device.getSupportedCommands()}", 1)
        
        /*metaConfig = setCommandsToHide(["on", "hiAgain2", "on"])
        metaConfig = setStateVariablesToHide(["uptime"], metaConfig=metaConfig)
        metaConfig = setCurrentStatesToHide(["needUpdate"], metaConfig=metaConfig)
        metaConfig = setDatasToHide(["namespace"], metaConfig=metaConfig)
        metaConfig = setPreferencesToHide(["port"], metaConfig=metaConfig)*/

        // This should be the first place we access metaConfig here, so clear and reset...
        metaConfig = clearThingsToHide()
        metaConfig = setCommandsToHide([], metaConfig=metaConfig)
        metaConfig = setStateVariablesToHide(['settings', 'colorMode', 'red', 'green', 'blue', 
            'mired', 'level', 'saturation', 'mode', 'hue'], metaConfig=metaConfig)
        
        metaConfig = setCurrentStatesToHide(['needUpdate'], metaConfig=metaConfig)
        //metaConfig = setDatasToHide(['preferences', 'namespace', 'appReturn', 'metaConfig'], metaConfig=metaConfig)
        metaConfig = setDatasToHide(['namespace', 'appReturn'], metaConfig=metaConfig)
        metaConfig = setPreferencesToHide([], metaConfig=metaConfig)
    }
    try {
        // In case we have some more to run specific to this driver
        refreshAdditional(metaConfig)
    } catch (MissingMethodException e1) {
        // ignore
        try {
            // In case we have some more to run specific to this driver
            refreshAdditional()
        } catch (MissingMethodException e2) {
            // ignore
        }
    }
}

void reboot() {
	logging("reboot()", 10)
    getAction(getCommandString("Restart", "1"))
}

// Call order: installed() -> configure() -> updated() 
void updated() {
    logging("updated()", 10)
    if(isDriver()) {
        logging("before updateNeededSettings()", 10)
        updateNeededSettings()
        logging("after updateNeededSettings()", 10)
        //sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, displayed: false, data: [protocol: "lan", hubHardwareId: device.hub.hardwareID])
        sendEvent(name:"needUpdate", value: device.currentValue("needUpdate"), displayed:false, isStateChange: false)
    }
    try {
        // Also run initialize(), if it exists...
        initialize()
        updatedAdditional()
    } catch (MissingMethodException e) {
        // ignore
    }
}

/*
    // Stress-testing runInstallCommands() 
    installCommands = []
    installCommands.add(["rule1", 'ON Var1#Value DO Var4 0; ENDON'])
    installCommands.add(["rule2", 'ON Var2#Value DO Var4 0; ENDON'])
    installCommands.add(["rule3", 'ON Var3#Value DO Var4 0; ENDON'])
    installCommands.add(["var1", "0"])
    installCommands.add(["var2", "0"])
    installCommands.add(["var3", "0"])
    (1..8).each {
        installCommands.add(["rule1", "+ ON Var1#Value DO Var4 $it; ENDON"])
        installCommands.add(["rule2", "+ ON Var2#Value DO Var4 $it; ENDON"])
        installCommands.add(["rule3", "+ ON Var3#Value DO Var4 $it; ENDON"])
        installCommands.add(["add1", "1"])
        installCommands.add(["add2", "1"])
        installCommands.add(["add3", "1"])
    }
    installCommands.add(["rule1", '0'])
    installCommands.add(["rule2", '0'])
    installCommands.add(["rule3", '0'])
    logging("refreshAdditional installCommands=$installCommands", 1)
    runInstallCommands(installCommands)
*/
void runInstallCommands(installCommands) {
    // Runs install commands as defined in helpers-device-configurations
    // Called from updateNeededSettings() in parent drivers
    logging("runInstallCommands(installCommands=$installCommands)", 1)
    List backlogs = []
    List rule1 = []
    List rule2 = []
    List rule3 = []
    installCommands.each {cmd->
        if(cmd[0].toLowerCase() == "rule1") {
            rule1.add([command: cmd[0], value:urlEscape(cmd[1])])
        } else if(cmd[0].toLowerCase() == "rule2") {
            rule2.add([command: cmd[0], value:urlEscape(cmd[1])])
        } else if(cmd[0].toLowerCase() == "rule3") {
            rule3.add([command: cmd[0], value:urlEscape(cmd[1])])
        } else {
            backlogs.add([command: cmd[0], value:urlEscape(cmd[1])])
        }
    }

    // Backlog inter-command delay in milliseconds
    getAction(getCommandString("SetOption34", "20"))
    pauseExecution(100)
    // Maximum 30 commands per backlog call
    while(backlogs.size() > 0) {
        getAction(getMultiCommandString(backlogs.take(10)))
        backlogs = backlogs.drop(10)
        // If we run this too fast Tasmota can't keep up, 1000ms is enough when 20ms between commands...
        if(backlogs.size() > 0) pauseExecution(1000)
        // REALLY don't use pauseExecution often... NOT good for performance...
    }

    [rule1, rule2, rule3].each {
        //logging("rule: $it", 1)
        it.each {rule->
            // Rules can't run in backlog!
            getAction(getCommandString(rule["command"], rule["value"]))
            //logging("cmd=${rule["command"]}, value=${rule["value"]}", 1)
            pauseExecution(100)
            // REALLY don't use pauseExecution often... NOT good for performance...
        }
    }
    getAction(getCommandString("SetOption34", "200"))
}

void updatePresence(String presence) {
    // presence - ENUM ["present", "not present"]
    logging("updatePresence(presence=$presence)", 1)
    Integer timeout = getTelePeriodValue()
    if(presence == "present") {    
        timeout += (timeout * 0.1 > 60 ? Math.round(timeout * 0.1) : 60)
        //log.warn "Setting as present with timeout: $timeout"
        runIn(timeout, "updatePresence", [data: "not present"])
    } else {
        log.warn "Presence time-out reached, setting device as 'not present'!"
    }
    sendEvent(name: "presence", value: presence, isStateChange: true, descriptionText: "No update received from the Tasmota device for ${timeout} seconds...")
}

Map parseDescriptionAsMap(description) {
    // Used by parse(description) to get descMap
	description.split(",").inject([:]) { map, param ->
		def nameAndValue = param.split(":")
        
        if (nameAndValue.length == 2) { 
            map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]
        } else {
            map += [(nameAndValue[0].trim()):""]
        }
	}
}

private getAction(uri, callback="parse") { 
    logging("Using getAction for '${uri}'...", 0)
    httpGetAction(uri, callback=callback)
}

def parse(asyncResponse, data) {
    // Parse called by default when using asyncHTTP
    if(asyncResponse != null) {
        try{
            logging("parse(asyncResponse.getJson() 2= \"${asyncResponse.getJson()}\", data = \"${data}\")", 1)
            parseResult(asyncResponse.getJson())
        } catch(MissingMethodException e1) {
            log.error e1
        } catch(e1) {
            try{
                logging("parse(asyncResponse.data = \"${asyncResponse.data}\", data = \"${data}\") e1=$e1", 1)
            } catch(e2) {
                logging("parse(asyncResponse.data = null, data = \"${data}\") Is the device online? e2=$e2", 1)
            }
        }
    } else {
        logging("parse(asyncResponse.data = null, data = \"${data}\")", 1)
    }
}


/*
    Methods related to configureChildDevices()

    configureChildDevices() detects which child devices to create/update and does the creation/updating
*/

void parseConfigureChildDevices(asyncResponse, data) {
    if(asyncResponse != null) {
        try{
            logging("parse(asyncResponse.getJson() 2= \"${asyncResponse.getJson()}\", data = \"${data}\")", 1)
            configureChildDevices(asyncResponse, data)
        } catch(MissingMethodException e1) {
            log.error e1
        } catch(e1) {
            try{
                logging("parse(asyncResponse.data = \"${asyncResponse.data}\", data = \"${data}\") e1=$e1", 1)
            } catch(e2) {
                logging("parse(asyncResponse.data = null, data = \"${data}\") Is the device online? e2=$e2", 1)
            }
        }
    } else {
        logging("parse(asyncResponse.data = null, data = \"${data}\")", 1)
    }
}

Boolean containsKeyInSubMap(aMap, key) {
    Boolean hasKey = false
    aMap.find {
        try{
            hasKey = it.value.containsKey(key)
        } catch(e) {

        }
        hasKey == true
    }
    return hasKey
}

Integer numOfKeyInSubMap(aMap, String key) {
    Integer numKeys = 0
    aMap.each {
        try{
            if(it.value.containsKey(key)) numKeys += 1
        } catch(e) {
            // Do nothing
        }
    }
    return numKeys
}

Integer numOfKeysIsMap(aMap) {
    Integer numKeys = 0
    aMap.each {
        if(it.value instanceof java.util.Map) numKeys += 1
    }
    return numKeys
}

TreeMap getKeysWithMapAndId(aMap) {
    def foundMaps = [:] as TreeMap
    aMap.each {
        if(it.value instanceof java.util.Map) {
            foundMaps[it.key] = it.value
        }
    }
    return foundMaps
}

void configureChildDevices(asyncResponse, data) {
    // This detects which child devices to create/update and does the creation/updating
    def statusMap = asyncResponse.getJson()
    logging("configureChildDevices() statusMap=$statusMap", 1)
    // Use statusMap to determine which Child Devices we should create

    // The built-in Generic Components are:
    //
    // Acceleration Sensor - ID: 189
    // Contact Sensor      - ID: 192
    // Contact/Switch      - ID: 199
    // CT                  - ID: 198
    // Dimmer              - ID: 187
    // Metering Switch     - ID: 188
    // Motion Sensor       - ID: 197
    // RGB                 - ID: 195
    // RGBW                - ID: 191
    // Smoke Detector      - ID: 196
    // Switch              - ID: 190
    // Temperature Sensor  - ID: 200
    // Water Sensor        - ID: 194

    // {"StatusSTS":{"Time":"2020-01-26T01:13:27","Uptime":"15T02:59:27","UptimeSec":1306767,
    // "Heap":26,"SleepMode":"Dynamic","Sleep":50,"LoadAvg":19,"MqttCount":0,"POWER1":"OFF",
    // "POWER2":"OFF","POWER3":"OFF","POWER4":"OFF","Wifi":{"AP":1,"SSId":"network",
    // "BSSId":"4A:11:11:12:CF:11","Channel":1,"RSSI":62,"LinkCount":37,"Downtime":"0T00:05:48"}}}

    // With a dimmer:
    // {"StatusSTS":{"Time":"2020-01-26T11:58:10","Uptime":"0T00:01:20","UptimeSec":80,"Heap":26,
    // "SleepMode":"Dynamic","Sleep":50,"LoadAvg":19,"MqttCount":0,"POWER":"OFF","Dimmer":0,
    // "Fade":"OFF","Speed":1,"LedTable":"ON","Wifi":{"AP":1,"SSId":"network",
    // "BSSId":"4A:11:11:12:D9:11","Channel":1,"RSSI":100,"LinkCount":1,"Downtime":"0T00:00:06"}}}

    // With an RGB+CW+WW light:
    // {"StatusSTS":{"Time":"2020-01-26T12:07:57","Uptime":"0T00:06:58","UptimeSec":418,"Heap":27,
    // "SleepMode":"Dynamic","Sleep":10,"LoadAvg":99,"MqttCount":0,"POWER":"ON","Dimmer":100,
    // "Color":"000000FF62","HSBColor":"0,0,0","Channel":[0,0,0,100,38],"CT":250,"Scheme":0,
    // "Fade":"ON","Speed":10,"LedTable":"ON","Wifi":{"AP":1,"SSId":"network",
    // "BSSId":"4A:11:11:12:D9:11","Channel":1,"RSSI":96,"LinkCount":1,"Downtime":"0T00:00:06"}}}
    

    // With an RGB+W light:
    // {"StatusSTS":{"Time":"2020-01-26T12:11:56","Uptime":"0T00:00:26","UptimeSec":26,"Heap":27,
    // "SleepMode":"Dynamic","Sleep":10,"LoadAvg":99,"MqttCount":0,"POWER":"ON","Dimmer":100,
    // "Color":"000000FF","HSBColor":"0,0,0","Channel":[0,0,0,100],"Scheme":0,"Fade":"ON",
    // "Speed":10,"LedTable":"ON","Wifi":{"AP":1,"SSId":"network","BSSId":"4A:11:11:12:D9:11",
    // "Channel":1,"RSSI":90,"LinkCount":1,"Downtime":"0T00:00:06"}}}

    // With an RGB light:
    // {"StatusSTS":{"Time":"2020-01-26T12:14:15","Uptime":"0T00:00:19","UptimeSec":19,"Heap":27,
    // "SleepMode":"Dynamic","Sleep":10,"LoadAvg":99,"MqttCount":0,"POWER":"ON","Dimmer":100,
    // "Color":"FFFFFF","HSBColor":"0,0,100","Channel":[100,100,100],"Scheme":0,"Fade":"ON",
    // "Speed":10,"LedTable":"ON","Wifi":{"AP":1,"SSId":"network","BSSId":"4A:11:11:12:D9:11",
    // "Channel":1,"RSSI":98,"LinkCount":1,"Downtime":"0T00:00:06"}}}

    // With CW+WW ("CT" is available):
    // {"StatusSTS":{"Time":"2020-01-26T12:16:48","Uptime":"0T00:00:17","UptimeSec":17,"Heap":28,
    // "SleepMode":"Dynamic","Sleep":10,"LoadAvg":99,"MqttCount":0,"POWER":"ON","Dimmer":100,
    // "Color":"FF62","HSBColor":"0,0,0","Channel":[100,38],"CT":250,"Fade":"ON","Speed":10,
    // "LedTable":"ON","Wifi":{"AP":1,"SSId":"network","BSSId":"4A:11:11:12:D9:11",
    // "Channel":1,"RSSI":94,"LinkCount":1,"Downtime":"0T00:00:06"}}}

    // With CW or WW (PWM1 configured on the correct pin), just the same as a normal dimmer...
    // {"StatusSTS":{"Time":"2020-01-26T12:19:51","Uptime":"0T00:01:15","UptimeSec":75,"Heap":27,
    // "SleepMode":"Dynamic","Sleep":10,"LoadAvg":99,"MqttCount":0,"POWER":"ON","Dimmer":71,
    // "Fade":"ON","Speed":10,"LedTable":"ON","Wifi":{"AP":1,"SSId":"network",
    // "BSSId":"4A:11:11:12:D9:11","Channel":1,"RSSI":88,"LinkCount":1,"Downtime":"0T00:00:25"}}}

    // Addressable RGB light (has the attribute "Width")
    // {"StatusSNS":{"Time":"2020-01-26T12:57:30","SR04":{"Distance":8.579}}}
    // {"StatusSTS":{"Time":"2020-01-26T12:57:30","Uptime":"0T00:02:14","UptimeSec":134,"Heap":21,
    // "SleepMode":"Dynamic","Sleep":10,"LoadAvg":113,"MqttCount":0,"POWER1":"ON","POWER2":"ON",
    // "Dimmer":100,"Color":"00FF00","HSBColor":"120,100,100","Channel":[0,100,0],"Scheme":13,
    // "Width":2,"Fade":"OFF","Speed":1,"LedTable":"ON","Wifi":{"AP":1,"SSId":"network",
    // "BSSId":"4A:11:11:12:D9:11","Channel":1,"RSSI":100,"Signal":-40,"LinkCount":1,
    // "Downtime":"0T00:00:09"}}}

    // {"StatusSNS":{"Time":"2020-01-26T01:24:16","BMP280":{"Temperature":23.710,"Pressure":1017.6},
    // "PressureUnit":"hPa","TempUnit":"C"}}

    // Multiple temperature sensors:
    // {"Time":"2020-01-26T17:45:30","DS18B20-1":{"Id":"000008BD38BF","Temperature":26.1},
    // "DS18B20-2":{"Id":"000008BD9714","Temperature":25.1},"DS18B20-3":{"Id":"000008C02C3A",
    // "Temperature":25.3},"TempUnit":"C"}
    
    // For DS18B20, us ID to distinguish them? Then you can't replace them...
    // For AM2301 the GPIO used is appended.
    // {"StatusSNS":{"Time":"2020-01-26T20:54:10","DS18B20-1":{"Id":"000008BD38BF","Temperature":25.8},
    // "DS18B20-2":{"Id":"000008BD9714","Temperature":24.7},"DS18B20-3":{"Id":"000008C02C3A","Temperature":24.9},
    // "AM2301-12":{"Temperature":25.1,"Humidity":66.4},"AM2301-14":{"Temperature":null,"Humidity":null},"TempUnit":"C"}}

    // D5 = GPIO14
    // D6 = GPIO12
    // D7 = GPIO13

    // Distance Sensor
    // {"StatusSNS":{"Time":"2020-01-26T13:52:19","SR04":{"Distance":11.667}}}

    // {"NAME":"ControlRGBWWCW","GPIO":[17,0,0,0,0,40,0,0,38,39,37,41,0],"FLAG":0,"BASE":18}

    // result: [Time:2020-01-30T11:30:43, DS18B20-1:[Id:000008BD38BF, Temperature:25.3], DS18B20-2:[Id:000008BD9714, Temperature:24.3], DS18B20-3:[Id:000008C02C3A, Temperature:24.4], AM2301-12:[Temperature:24.2, Humidity:68.1], AM2301-14:[Temperature:24.0, Humidity:68.1], TempUnit:C]
    // result: [Time:2020-01-30T11:31:12, DS18B20-1:[Id:000008BD38BF, Temperature:25.3], DS18B20-2:[Id:000008BD9714, Temperature:24.3], DS18B20-3:[Id:000008C02C3A, Temperature:24.4], AM2301-12:[Temperature:24.2, Humidity:68.0], AM2301-14:[Temperature:24.0, Humidity:68.1], TempUnit:C]
    // [hasEnergy:false, numTemperature:5, numHumidity:2, numPressure:0, numDistance:0, sensorMap:[AM2301-12:[Temperature:24.2, Humidity:68.1], AM2301-14:[Temperature:24.0, Humidity:68.1], DS18B20-1:[Id:000008BD38BF, Temperature:25.3], DS18B20-2:[Id:000008BD9714, Temperature:24.3], DS18B20-3:[Id:000008C02C3A, Temperature:24.4]], numSwitch:0, isDimmer:false, isAddressable:false, isRGB:false, hasCT:false]

    // SENSOR = {"Time":"2020-01-30T19:15:08","SR04":{"Distance":73.702}}

    // Switch or Metering Switch are the two most likely ones
    def deviceInfo = [:]
    deviceInfo["hasEnergy"] = false
    deviceInfo["numTemperature"] = 0
    deviceInfo["numHumidity"] = 0
    deviceInfo["numPressure"] = 0
    deviceInfo["numDistance"] = 0
    deviceInfo["numSensorGroups"] = 0
    deviceInfo["sensorMap"] = [:]
    if(statusMap.containsKey("StatusSNS")) {
        sns = statusMap["StatusSNS"]
        deviceInfo["hasEnergy"] = sns.containsKey("ENERGY")
        deviceInfo["sensorMap"] = getKeysWithMapAndId(sns)
        deviceInfo["numSensorGroups"] = deviceInfo["sensorMap"].size()
        deviceInfo["numTemperature"] = numOfKeyInSubMap(sns, "Temperature")
        deviceInfo["numHumidity"] = numOfKeyInSubMap(sns, "Humidity")
        deviceInfo["numPressure"] = numOfKeyInSubMap(sns, "Pressure")
        deviceInfo["numDistance"] = numOfKeyInSubMap(sns, "Distance")
    }

    deviceInfo["numSwitch"] = 0
    deviceInfo["isDimmer"] = false
    deviceInfo["isAddressable"] = false
    deviceInfo["isRGB"] = false
    deviceInfo["hasCT"] = false
    if(statusMap["StatusSTS"] != null) {
        sts = statusMap["StatusSTS"]
        deviceInfo["isDimmer"] = sts.containsKey("Dimmer")
        deviceInfo["isAddressable"] = sts.containsKey("Width")
        if(sts.containsKey("Color")) deviceInfo["isRGB"] = sts["Color"].length() >= 6
        deviceInfo["hasCT"] = sts.containsKey("CT")

        if(sts["POWER"] != null) {
            // This only exist if there is ONLY one switch/bulb
            deviceInfo["numSwitch"] = 1
        } else {
            i = 1
            while(sts["POWER$i"] != null) {
                i += 1
            }
            deviceInfo["numSwitch"] = i - 1
        }
    }
    logging("Device info found: $deviceInfo", 100)

    // Create the devices, if needed

    // Switches
    def driverName = ["Tasmota - Universal Switch (Child)", "Generic Component Switch"]
    def namespace = "tasmota"
    if(deviceInfo["numSwitch"] > 0) {
        if(deviceInfo["numSwitch"] > 1 && (
            deviceInfo["isDimmer"] == true || deviceInfo["isAddressable"] == true || 
            deviceInfo["isRGB"] == true || deviceInfo["hasCT"] == true)) {
                log.warn "There's more than one switch and the device is either dimmable, addressable, RGB or has CT capability. This is not fully supported yet, please report which device and settings you're using to the developer."
        }
        if(deviceInfo["hasEnergy"] && (deviceInfo["isAddressable"] == false && deviceInfo["isRGB"] == false && deviceInfo["hasCT"] == false)) {
            if(deviceInfo["isDimmer"]) {
                // TODO: Make a Component Dimmer with Metering
                driverName = ["Tasmota - Universal Dimmer (Child)", "Generic Component Dimmer"]
            } else {
                driverName = ["Tasmota - Universal Switch (Child)", "Generic Component Metering Switch"]
            }
        } else {
            if(deviceInfo["hasEnergy"]) {
                log.warn "This device reports Metering Capability AND has RGB, Color Temperature or is Addressable. Metering values will be ignored... This is NOT supported and may result in errors, please report it to the developer."
            }
            if((deviceInfo["isDimmer"] == true || deviceInfo["isAddressable"] == true || 
                deviceInfo["isRGB"] == true || deviceInfo["hasCT"] == true)) {
                if(deviceInfo["isAddressable"] == false && deviceInfo["isRGB"] == false && deviceInfo["hasCT"] == false) {
                    driverName = ["Tasmota - Universal Dimmer (Child)", "Generic Component Dimmer"]
                } else if(deviceInfo["isAddressable"] == false && deviceInfo["isRGB"] == false && deviceInfo["hasCT"] == true) {
                    driverName = ["Tasmota - Universal CT/RGB/RGB+CW+WW (Child)", "Generic Component CT"]
                } else if(deviceInfo["isRGB"] == true && deviceInfo["hasCT"] == false) {
                    driverName = ["Tasmota - Universal CT/RGB/RGB+CW+WW (Child)", "Generic Component RGB"]
                } else {
                    driverName = ["Tasmota - Universal CT/RGB/RGB+CW+WW (Child)", "Generic Component RGBW"]
                }
            }
        }
        
        for(i in 1..deviceInfo["numSwitch"]) {
            namespace = "tasmota"
            def childId = "POWER$i"
            def childName = getChildDeviceNameRoot(keepType=true) + " ${getMinimizedDriverName(driverName[0])} ($childId)"
            def childLabel = "${getMinimizedDriverName(device.getLabel())} ($childId)"
            logging("createChildDevice: POWER$i", 1)
            createChildDevice(namespace, driverName, childId, childName, childLabel)
            // Once the first switch is created we only support one type... At least for now...
            driverName = ["Tasmota - Universal Switch (Child)", "Generic Component Switch"]
        }
    }
    
    // Sensors
    logging("Available in sensorMap: ${deviceInfo["sensorMap"]}, size:${deviceInfo["numSensorGroups"]}", 0)
    deviceInfo["sensorMap"].each {
        logging("sensorMap: $it.key", 0)
        namespace = "tasmota"
        driverName = ["Tasmota - Universal Multisensor (Child)"]
        def childId = "${it.key}"
        def childName = getChildDeviceNameRoot(keepType=true) + " ${getMinimizedDriverName(driverName[0])} ($childId)"
        def childLabel = "${getMinimizedDriverName(device.getLabel())} ($childId)"
        createChildDevice(namespace, driverName, childId, childName, childLabel)
    }
    //logging("After sensor creation...", 0)
    // Finally let the default parser have the data as well...
    parseResult(statusMap)
}

String getChildDeviceNameRoot(Boolean keepType=false) {
    String childDeviceNameRoot = getDeviceInfoByName('name')
    if(childDeviceNameRoot.toLowerCase().endsWith(' (parent)')) {
        childDeviceNameRoot = childDeviceNameRoot.substring(0, childDeviceNameRoot.length()-9)
    } else if(childDeviceNameRoot.toLowerCase().endsWith(' parent')) {
        childDeviceNameRoot = childDeviceNameRoot.substring(0, childDeviceNameRoot.length()-7)
    }
    if(keepType == false && childDeviceNameRoot.toLowerCase().startsWith('tasmota - ')) {
        childDeviceNameRoot = childDeviceNameRoot.substring(10, childDeviceNameRoot.length())
    }
    return childDeviceNameRoot
}

String getMinimizedDriverName(String driverName) {
    // Remove parts we don't need from the string 
    logging("getMinimizedDriverName(driverName=$driverName)", 1)
    if(driverName.toLowerCase().endsWith(' (child)')) {
        driverName = driverName.substring(0, driverName.length()-8)
    } else if(driverName.toLowerCase().endsWith(' child')) {
        driverName = driverName.substring(0, driverName.length()-6)
    }
    if(driverName.toLowerCase().endsWith(' (parent)')) {
        driverName = driverName.substring(0, driverName.length()-9)
    } else if(driverName.toLowerCase().endsWith(' parent')) {
        driverName = driverName.substring(0, driverName.length()-7)
    }
    if(driverName.toLowerCase().startsWith('tasmota - ')) {
        driverName = driverName.substring(10, driverName.length())
    }
    if(driverName.toLowerCase().startsWith('universal ')) {
        driverName = driverName.substring(10, driverName.length())
    }
    driverName = driverName.replaceAll("Generic Component ", "")
    logging("getMinimizedDriverName(driverName=$driverName) end", 1)
    return driverName
}

def getChildDeviceByActionType(String actionType) {
    return childDevices.find{it.deviceNetworkId.endsWith("-$actionType")}
}

private void createChildDevice(String namespace, List driverName, String childId, String childName, String childLabel) {
    logging("createChildDevice(namespace=$namespace, driverName=$driverName, childId=$childId, childName=$childName, childLabel=$childLabel)", 1)
    def childDevice = childDevices.find{it.deviceNetworkId.endsWith("-$childId")}
    if(!childDevice && childId.toLowerCase().startsWith("power")) {
        // If this driver was used to replace an "old" parent driver, rename the child Network ID
        logging("Looking for $childId, ending in ${childId.substring(5)}", 1)
        childDevice = childDevices.find{it.deviceNetworkId.endsWith("-${childId.substring(5)}")}
        if(childDevice) {
            logging("Setting new Network ID for $childId to '$device.id-$childId'", 1)
            childDevice.setDeviceNetworkId("$device.id-$childId")
        }
    }
    if (childDevice) {
        // The device exists, just update it
        childDevice.setName(childName)
        logging(childDevice.getData(), 10)
    } else {
        logging("The child device doesn't exist, create it...", 0)
        Integer s = childName.size()
        for(i in 0..s) {
            def currentNamespace = namespace
            if(driverName[i].toLowerCase().startsWith('generic component')) {
                currentNamespace = "hubitat"
            }
            try {
                addChildDevice(currentNamespace, driverName[i], "$device.id-$childId", [name: childName, label: childLabel, isComponent: false])
                logging("Created child device '$childLabel' using driver '${driverName[i]}'...", 100)
            } catch (com.hubitat.app.exception.UnknownDeviceTypeException e) {
                if(i == s) {
                    log.error "'${driverName[i]}' driver can't be found! Did you forget to install the child driver?"
                } else {
                    log.warn "'${driverName[i]}' driver can't be found! Trying another driver: ${driverName[i+1]}..."
                }
            }
        }
    }
}

/*
    Tasmota IP Settings and Wifi status
*/
private String setDeviceNetworkId(String macOrIP, boolean isIP = false) {
    String myDNI
    if (isIP == false) {
        myDNI = macOrIP
    } else {
        logging("About to convert ${macOrIP}...", 0)
        myDNI = convertIPtoHex(macOrIP)
    }
    logging("Device Network Id should be set to ${myDNI} from ${macOrIP}", 0)
    return myDNI
}

void prepareDNI() {
    // Called from updateNeededSettings() and parse(description)
    if (useIPAsID) {
        def hexIPAddress = setDeviceNetworkId(ipAddress, true)
        if(hexIPAddress != null && state.dni != hexIPAddress) {
            state.dni = hexIPAddress
            updateDNI()
        }
    } else if (state.mac != null && state.dni != state.mac) { 
        state.dni = setDeviceNetworkId(state.mac)
        updateDNI()
    }
}

private void updateDNI() {
    // Called from:
    // preapreDNI()
    // httpGetAction(uri, callback="parse")
    // postAction(uri, data)
    if (state.dni != null && state.dni != "" && device.deviceNetworkId != state.dni) {
        logging("Device Network Id will be set to ${state.dni} from ${device.deviceNetworkId}", 0)
        device.deviceNetworkId = state.dni
    }
}

Integer getTelePeriodValue() {
    // Naming this getTelePeriod() will cause Error 500 and other unexpected behavior when
    // telePeriod isn't set to anything...
    return (telePeriod != null && telePeriod.isInteger() ? telePeriod.toInteger() : 300)
}

private String getHostAddress() {
    if (port == null) {
        port = 80
    }
    if (override == true && ipAddress != null){
        return "${ipAddress}:${port}"
    }
    else if(getDeviceDataByName("ip") && getDeviceDataByName("port")){
        return "${getDeviceDataByName("ip")}:${getDeviceDataByName("port")}"
    }else{
	    return "${ip}:80"
    }
}

private String convertIPtoHex(ipAddress) {
    String hex = null
    if(ipAddress != null) {
        hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02X', it.toInteger() ) }.join()
        logging("Get this IP in hex: ${hex}", 0)
    } else {
        hex = null
        if (useIPAsID) {
            logging('ERROR: To use IP as Network ID "Device IP Address" needs to be set and "Override IP" needs to be enabled! If this error persists, consult the release thread in the Hubitat Forum.')
        }
    }
    return hex
}

void sync(ip, port = null) {
    String existingIp = getDataValue("ip")
    String existingPort = getDataValue("port")
    logging("Running sync()", 1)
    if (ip && ip != existingIp) {
        updateDataValue("ip", ip)
        sendEvent(name: 'ip', value: ip)
        sendEvent(name: "ipLink", value: "<a target=\"device\" href=\"http://$ip\">$ip</a>")
        logging("IP set to ${ip}", 1)
    }
    if (port && port != existingPort) {
        updateDataValue("port", port)
        logging("Port set to ${port}", 1)
    }
}

Integer dBmToQuality(Integer dBm) {
    Integer quality = 0
    if(dBm > 0) dBm = dBm * -1
    if(dBm <= -100) {
        quality = 0
    } else if(dBm >= -50) {
        quality = 100
    } else {
        quality = 2 * (dBm + 100)
    }
    logging("DBM: $dBm (${quality}%)", 0)
    return quality
}

/*
    Tasmota Preferences Related
*/
String configuration_model_tasmota() {
'''
<configuration>
<Value type="password" byteSize="1" index="password" label="Device Password" description="REQUIRED if set on the Device! Otherwise leave empty." min="" max="" value="" setting_type="preference" fw="">
<Help>
</Help>
</Value>
</configuration>
'''
}

/*
    HTTP Tasmota API Related
*/
private void httpGetAction(uri, callback="parse") { 
  updateDNI()
  
  def headers = getHeader()
  logging("Using httpGetAction for 'http://${getHostAddress()}$uri'...", 0)
  try {
    /*hubAction = new hubitat.device.HubAction(
        method: "GET",
        path: uri,
        headers: headers
    )*/
    asynchttpGet(
        callback,
        [uri: "http://${getHostAddress()}$uri",
        headers: headers]
    )
  } catch (e) {
    log.error "Error in httpGetAction(uri): $e ('$uri')"
  }
}

private postAction(uri, data) { 
  updateDNI()

  def headers = getHeader()

  def hubAction = null
  try {
    hubAction = new hubitat.device.HubAction(
    method: "POST",
    path: uri,
    headers: headers,
    body: data
  )
  } catch (e) {
    log.error "Error in postAction(uri, data): $e ('$uri', '$data')"
  }
  return hubAction    
}

String getCommandString(command, value) {
    def uri = "/cm?"
    if (password) {
        uri += "user=admin&password=${password}&"
    }
	if (value) {
		uri += "cmnd=${command}%20${value}"
	}
	else {
		uri += "cmnd=${command}"
	}
    return uri
}

String getMultiCommandString(commands) {
    String uri = "/cm?"
    if (password) {
        uri += "user=admin&password=${password}&"
    }
    uri += "cmnd=backlog%20"
    if(commands.size() > 30) {
        log.warn "Backlog only supports 30 commands, the last ${commands.size() - 30} will be ignored!"
    }
    commands.each {cmd->
        if(cmd.containsKey("value")) {
          uri += "${cmd['command']}%20${cmd['value']}%3B%20"
        } else {
          uri += "${cmd['command']}%3B%20"
        }
    }
    return uri
}

private String urlEscape(url) {
    return(URLEncoder.encode(url).replace("+", "%20"))
}

private String convertPortToHex(port) {
	String hexport = port.toString().format( '%04X', port.toInteger() )
    return hexport
}

private encodeCredentials(String username, String password) {
	String userpassascii = "${username}:${password}"
    String userpass = "Basic " + userpassascii.bytes.encodeBase64().toString()
    return userpass
}

private Map getHeader(userpass = null) {
    Map headers = [:]
    headers.put("Host", getHostAddress())
    headers.put("Content-Type", "application/x-www-form-urlencoded")
    if (userpass != null)
       headers.put("Authorization", userpass)
    return headers
}

/**
 * --END-- TASMOTA METHODS (helpers-tasmota)
 */

/**
 * STYLING (helpers-styling)
 *
 * Helper functions included in all Drivers and Apps using Styling
 */
String addTitleDiv(title) {
    return '<div class="preference-title">' + title + '</div>'
}

String addDescriptionDiv(description) {
    return '<div class="preference-description">' + description + '</div>'
}

String makeTextBold(s) {
    // DEPRECATED: Should be replaced by CSS styling!
    if(isDriver()) {
        return "<b>$s</b>"
    } else {
        return "$s"
    }
}

String makeTextItalic(s) {
    // DEPRECATED: Should be replaced by CSS styling!
    if(isDriver()) {
        return "<i>$s</i>"
    } else {
        return "$s"
    }
}

/**
 * --END-- STYLING METHODS (helpers-styling)
 */
