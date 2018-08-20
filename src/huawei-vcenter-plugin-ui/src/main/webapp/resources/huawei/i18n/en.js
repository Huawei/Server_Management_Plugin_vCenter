var i18n_en = {
    common: {
        Close: 'Close',
        confirm: 'OK',
        cancel: 'Cancel',
        clear: 'Clear',
        prompt: 'Prompt',
        delete: 'Delete',
        requiredErrorMsg: 'Mandatory field',
        operation: 'Operation',
        advanced: 'Advanced',
        advancedSet: 'Advanced Settings',
        add: 'Add',
        reset: 'Reset',
        search: 'Search',
        notify: 'Notify',
        edit: 'Edit',
        status: 'Status',
        pleaseEnter: 'Please enter ',
        pleaseSelect: 'Please select ',
        confirmDelete: 'Are you sure to delete?',
        confirmDelete1: 'Are you sure you want to delete the selected data?',
        operateSucceeded: 'Delete successfully',
        addSucceeded: 'Add successfully',
        submit: 'Submit',
        back: 'Back',
        server: 'Server',
        template: 'Template',
        beBackTips: 'Return to the list page?',
        create: 'Create',
        operation: 'Operation',
        name: 'Name',
        required: "Required",
        refresh: 'Refresh',
        deleteResultTips: '# successful, & failed'
    },
    template: {
        templateOptin: { value: '', label: 'All template' },
        createTemplate: 'Create Template',
        getListFailed: 'Failed to obtain data',
        deletingTips: 'Deleting',
        savingTips: 'Saving',
        beforeDeleteTips: 'This operation will permanently delete the template. Do you want to continue?',
        addSuccessMsg: 'Add successfully',
        addFailedMsg: 'Add failed',
        delSuccessMsg: 'Delete successfully',
        editSuccessMsg: 'Edit successfully',
        delFailedMsg: 'Delete failed',
        templateType: 'Configuration Type',
        templateName: 'Name',
        templateDesc: 'Description',
        powerControl: 'Power Control',
        powerOn: 'Power On',
        powerOff: 'Power Off',
        restart: 'Restart',
        osType: 'OS Type',
        Software: 'Software',
        password: 'Administrator Password',
        confirmPassword: 'Confirm Password',
        deployDevice: 'Deploy Device',
        deployDeviceTips: 'In ServiceCD mode, the system changes the preferred option of BIOS boot to the current location of the operating system.',
        cdKey: 'The CD Key can contain a maximum of five characters, including uppercase letters and digits.',
        osSettings: 'OS Settings',
        partitionSettings: 'Partition Settings',
        addHardDisk: 'Add Hard Disk',
        deleteHardDisk: 'Delete Hard Disk',
        partition: 'Partition',
        mountPoint: 'Mount Point',
        fileSystem: 'File System',
        capacity: 'Capacity',
        operation: 'Operation',
        useAvailableCapacity: 'Use available capacity',
        templateNameErrorMsg: 'The name contains 6 to 32 characters, which can include chinese, letters, digits, hyphens (-)and underscores (_).',
        passWordErrorMsg: 'The administrator password must contain 8 to 32 characters, including at least any two types of the following: uppercase letters, lowercase letters, digits, and special characters including `~!@$%^&*()-_=+|[{}];:\'"",<.>/?',
        moreHardDiskErrorMsg1: 'The maximum of partition is 20.',
        moreHardDiskErrorMsg2: 'The maximum of partition is 14.',
        confirmPasswordErrorMsg: 'The two passwords must be the same.',
        confirmPasswordErrorMsg1: 'The confirm password must contain 8 to 32 characters!',
        beforeColsePageTips: 'Confirm close window?',
        capacityErrorRangeC: ' The capacity of drive C ranges from 32\,000 MB to 2\,000\,000 MB\, or from 32 GB to 2000 GB.',
        capacityErrorFat32: 'The capacity range for the FAT32 file system is 100 MB to 32000 MB or 1 GB to 32 GB\, and the free capacity cannot be used.',
        capacityErrorOther: 'The capacity range for the NTFS file system in Windows or a non-root partition in Linux is 100 MB to 999999 MB or 1 GB to 1000 GB.',
        capacityErrorRoot: 'The root directory capacity ranges from 10\,000 MB to 999\,999 MB or 10 GB to 1000 GB.',
        setBootOrder: 'Set the system boot order.',
        choiceSoftware: 'Select Software Source',
        choiceSoftware1: 'Please select software',
        softwareName: 'Name',
        softwareDesc: 'Description',
        softwareType: 'Type',
        wwpnErrorMsg: 'Format: XX:XX:XX:XX:XX:XX:XX:XX, X is a hexadecimal integer. Example: 1A:2A:3A:4A:5A:6A:7A:8A',
        lunErrorMsg: "Must be an integer between 0-255",
        adapterModel: 'Model',
        slot: 'Slot',
        advanced: 'Advanced',
        SANProperties: 'SAN Properties',
        enabled: 'Enabled',
        disabled: 'Disabled',
        priority: 'Priority',
        adapterModelErrorMsg: "Please select an adapter model.",
        systemBootOption: "System Boot Setting",
        serviceSettings: "Service Settings",
        NTPServerSource: "NTP Server Source",
        ntpStatus: "NTP Status",
        primaryNtpServer: "Primary NTP Server",
        secondaryNtpServer: "Secondary NTP Server",
        ipErrorMsg: "The IP address of the first only as an integer from 1 to 223, the remainder only is an integer of 0 to 255, and the first is not 127, not 0 as the last.",
        dnsSource: "DNS Obtain Mode",
        AutomaticallyObtain: "Automatically Obtain",
        ManuallySet: "Manually Set",
        domainName: "Domain Name",
        primaryDnsServer: "Primary DNS Server",
        secondaryDnsServer: "Secondary DNS Server",
        ldapStatus: "LDAP Status",
        domainControllerAddress: "Domain Controller Address",
        userDomain: "User Domain",
        roleGroup: "Role Group",
        groupName: "Group Name",
        groupDomain: "Group Domain",
        groupPrivilege: "Group Privilege",
        operation: "Operation",
        moreTableErrorMsg: "Create up to five role groups at most",
        userDomainErrorMsg: "Please enter a 1~255 format for the CN=XXX,DC=XXX,DC=XXX characters, which XXX cannot contain #',\"&, and a space, you can have any number of DC=XXX.",
        groupDomainErrorMsg: "Invalid group domain. The group domain must contain 1 to 255 characters in the format of CN=XXX\,OU=XXX\,DC=XXX\,DC=XXX\, where XXX cannot contain special characters #',\"& or spaces. The group domain can contain multiple DC=XXX.",
        roleGroupErrorMsg: "Please create at least one role group",
        deleteOriginalRAID: 'Delete Original RAID ',
        globalHotSpareDisk: 'Global Hot Spare Disk ',
        RAIDAdapterModel: 'RAID Adapter Model ',
        RAIDGroup: 'RAID Group',
        RAIDGroupLevel: 'RAID Group Level  ',
        privateHotSpareDisk: 'Private Hot Spare Disk ',
        diskGroupQuantity: 'Disk Group Quantity',
        LUNID: 'LUN ID',
        startLUNID: 'Start LUN ID',
        createRAIDGroup: 'Create RAID Group',
        RAIDlevel: 'RAID Level',
        privateHotSpareDisk: 'Private Hot Spare Disk',
        slotID: 'Slot ID',
        diskgroup: 'Disk Group',
        viewRAIDGroup: 'View RAID Group',
        RAIDDetails: 'RAID Details',
        writePolicy: 'Write Policy',
        readPolicy: 'Read Policy',
        IOPolicy: 'I/O Policy',
        LUNInformation: 'LUN Information',
        divisionMode: 'Division Mode',
        raid_capacity: 'Capacity(MB)',
        maximumAvailableCapacity: "Maximum Available Capacity",
        RAIDSubmitAlertMsg: "Please create at least one RAID group.",
        addRAIDGroupErrorMsg: "The model LSI2308(SR120/RU120) and LSI3008(SR130/RU130), up to only create 2 raid groups.",
        addLUNErrorMsg: "The maximum number of LUN is 64.",
        addDiskGroupErrorMsg: "The maximum of disk is 14.",
        createRaidErrorMsg: "Please enter the correct slot ID.",
        createRaidErrorMsg2: "Please select a RAID group level.",
        editRaidErrorMsg: "Please allocate a valid capacity to LUN |.",
        validateRAIDDiskGorupErrorMsg: "At least two disks is must be choose.",
        validateRAIDDiskGorupErrorMsg2: "If a RAID 0, RAID 1, RAID 5, RAID 6, or RAID 1E group is created, only one disk group can be created.",
        validateRAIDDiskGorupErrorMsg3: "At least one disks is must be choose.",
        validateRAIDDiskGorupErrorMsg4: "A RAID 1 group supports two disks only.",
        validateRAIDDiskGorupErrorMsg5: "At least three disks is must be choose.",
        validateRAIDDiskGorupErrorMsg6: "At least four disks is must be choose.",
        validateRAIDDiskGorupErrorMsg7: "A RAID 1E group must contain more than three disks and the number of disks must be odd.",
        validateRAIDDiskGorupErrorMsg8: "A RAID 10 group contains at least two disk groups , eight groups at most and each disk group can contain two disks only.",
        validateRAIDDiskGorupErrorMsg9: "A RAID 50 group contains at least two disk groups and each disk group contains at least three disks. The number of disks in each disk group must be the same.",
        validateRAIDDiskGorupErrorMsg10: "A RAID 60 group contains at least two disk groups and each disk group contains at least four disks. The number of disks in each disk group must be the same.",
        validateRAIDDiskGorupErrorMsg11: "After global hot spare disks are configured, you need to create other RAID groups besides RAID 0 groups.",
        softSourceVersion: 'Software Source Version',
        templateDescErrorMsg: "TemplateDesc must be between 0 and 128 letters.",
        mountPointErrorMsg: 'Mount points cannot be empty!',
        mountPointErrorMsg1: 'Mount points cannot be repeated!',
        mountPointErrorMsg2: 'The mount point can contain a maximum of 255 characters. The value must start with a slash (/), followed by digits\, letters\, and underscores (_).',
        viewDetail: 'View Template Details',
        extendSoftware:'Extension Software',
        extendSoftwareTips:'iBMA can be used to query the versions of drivers and in-band firmware such as RAID controller cards and CNAs,and upgrade the drivers. Only V3 and V5 servers support this function.'
    },
    vCenter: {
        save: 'Save',
        userName: 'vCenter UserName:',
        password: 'vCenter Password:',
        hostIp: 'vCenter IP:',
        saveDone: 'Save done.',
        saveDoneFail: 'Register HA Provider failed. Please check the environment.',
        saveDoneFail1: 'Some cancellation of the subscription failed. Please check and save again.',
        SaveSuccessInformation:'Unsubscribe from successful information',
        SaveFailInformation:'Unsubscribe from failed information',
        saveFail: 'OperationFailed: Please verify userName and password.',
        successTitle: 'Register HA Provider success information',
        saveDoneSuccess: 'Saved successfully',
        saveDoneSuccessHA: 'Register HA Provider successfully.',
        failTitle: 'Register HA Provider failure information',
        userNameError: 'The UserName contains 1 to 100 characters.',
        passwordError: 'The password contains 1 to 100 characters, which can include letters, digits, and special characters.',
        userNameNull: 'UserName cannot be null.',
        passwordNull: 'Password cannot be null.',
        changeInfo: 'Change password',
        stateInfo: 'Register HA Provider',
        loadingText:'Being configured, please wait...',
        prompt:'prompt',
        tableAliasName :'Alias Name',
        tableESightIP   :'eSightIP',
        systemId :'SystemId',
        ok:'OK',
        alertMsg:'Please configure eSight first',
        HAVersionSupport:'HA can not work in this version'
    },
    eSight: {
        search: 'Search',
        addeSight: 'Add eSight',
        editSight: 'Edit eSight',
        templateNum: 'CodeNum',
        templateHost: 'IP',
        templateAliasName: 'Alias Name',
        templatePort: 'Port',
        templateSystemId: 'SystemId',
        templateName: 'Name',
        templateDate: 'Created At',
        templateUpdate: 'Last Update',
        templateHandle: 'Operation',
        Edit: 'Edit',
        Delete: 'Delete',
        batchDelete: 'Batch Delete',
        eSightHost: 'Host:',
        eSightAliasName: 'Alias Name:',
        eSightPort: 'Port:',
        eSightName: 'Name:',
        eSightPassWord: 'Password:',
        eSightSystemId: 'SystemId:',
        linkTest: 'Test',
        SaveCong: 'Save',
        getListFailed: 'Failed to obtain data',
        linkTestSucc: 'Link test successfully',
        linkTestFalse: 'Link test failed',
        hostNull: 'Host cannot be null',
        aliasNameNull: 'AliasName cannot be null',
        portNull: 'Port cannot be null',
        systemIdNull: 'SystemId cannot be null',
        systemIdLength: 'Length should not exceed 50 characters',
        nameNull: 'Name cannot be null',
        pwdNull: 'Password cannot be null',
        legthCheck: 'Length should not exceed 100 characters',
        legthCheckMore: 'Port must between 0 and 65535',
        deleteTips: 'Are you sure to delete?',
        batchDeleteTips: 'Are you sure you want to delete the selected data?',
        deleteSucc: 'Delete successfully',
        deleteFail: 'Delete failed',
        addSucc: 'Add successfully',
        addFail: 'Add failed',
        editSucc: 'Edit successfully',
        hostIpIsRepeat: 'hostIp Already exists, please do not repeat the addition',
        aliasNameIsRepeat: 'alias Already exists, please do not repeat the addition ',
        aliasNameError: 'The aliasname contains 1 to 100 characters, which can include letters, digits, hyphens (-), underscores (_), and periods (.).',
        hostIPError: 'The first byte of the host IP address must be an integer ranging from 1 to 223 except 127. The other three bytes must be integers ranging from 0 to 255\, and the last byte cannot be 0.',
        userNameError: 'The loginAccount contains 1 to 100 characters, which can include letters, digits, hyphens (-), underscores (_), and periods (.).',
        passWordErrorMsg: 'The password contains 1 to 100 characters, which can include letters, digits,  and special characters',
        changeInfo: 'Change Credentials'
    },
    serverList: {
        serverGetListFailed: 'Failed to obtain data',
        serverIPAddress: 'IP Address',
        serverName: 'Name',
        serverStatus: 'Status',
        serverType: 'Type',
        serverHandle: 'Operation',
        serverEdit: 'Edit',
        serverDelete: 'Delete',
        serverSearch: 'Search',
        serverDeleteTips: 'This will permanently delete the server information, whether to continue?',
        serverDeleteSucc: 'Delete successfully',
        serverTemplateOptin: {
            value: '',
            label: 'All'
        },
        serverNormal: 'Normal',
        serverOffline: 'Offline',
        serverUnknown: 'Unknown',
        serverFault: 'Faulty',
        serverMode: 'ServerMode:',
        serverRack: 'Rack',
        serverBlade: 'Blade',
        serverHighdensity: 'High-density',
        serverStoragenode: 'Storagenode',
        serverThirdpartyserver: 'Thirdpartyserver',
        cpuHealthState: 'CPU Health Status',
        memoryHealthState: 'Memory Health Status'
    },
    firmware: {
        clearFailedTask: 'Clear Failed Task',
        packageName: 'Package Name',
        packageType: 'Package Type',
        esightHost: 'eSight Host',
        progress: 'Progress',
        status: 'Status',
        date: 'Date',
        packageDesc: 'Description',
        firmwareType: 'Firmware Type',
        firmwareVersion: 'Firmware Version',
        supportedDeviceModels: 'Supported Device Models',
        fileListL: 'File List',
        sftpPort: 'SFTP Port',
        sftpIp: 'SFTP IP',
        sftpUserName: 'SFTP User Name',
        sftpPassword: 'SFTP Password',
        basepackageNameErrorMsg: 'The name contains 6 to 32 characters\, which can include letters\, digits\, hyphens (-)\, and underscores (_)\.',
        sftpUserNameErrorMsg: 'The name must contain 0 to 64 characters',
        passWordErrorMsg: 'The password must contain 0 to 64 characters', //'The password must contain 8 to 32 characters, including at least any two types of the following: uppercase letters, lowercase letters, digits, and special characters including `~!@$%^&*()-_=+|[{}];:\'"",<.>/?',
        basepackageDescriptionErrorMsg: 'The name contains 0 to 128 characters',
        addSuccessMsg: 'Add successfully',
        deleteTips: 'Are you sure to delete?',
        deleteTips1: 'Are you sure to delete?',
        deleteSucc: 'Delete successfully',
        deleteSucc1: 'Delete successfully',
        deleteFail: 'Delete failed',
        firmwareDetails_packageName: 'Package Name',
        firmwareDetails_support: ' Supported OS Or Device Type',
        firmwareDetails_releaseDate: 'Release Date',

        createTask: 'Create Task',
        taskName: 'Name',
        bujianlist: 'Component List',
        taskProgress: 'Progress',
        taskResult: 'Status',
        createTime: 'Created At',
        taskStatusOptin: {
            value: '',
            label: 'All'
        },
        deleteTaskTips: 'Are you sure to delete?',
        basepackageNameTips: 'Please select the upgrade pack',
        firmwareListTips: 'Please select update firmware',
        dntips: 'Please select the server',
        selectPackage: 'Select Upgrade Firmware Package',
        selectFirmware: 'Select Upgrade Firmware',
        selectServer: 'Select Server',
        fileListLErrorMsg: 'Please enter File list.',
        fileListLErrorMsg1: 'The number of files has reached the maximum2.',
        selectupgradePolicy: 'Select Upgrade Policy',
        SelectEffectiveMode: 'Select Effective Mode',
        selectupgradePolicyItem: 'Non-forcible Device Firmware Type Matching',
        selectupgradePolicyTips: 'If selected Non-forcible device firmware type matching, the system does not check whether the device contains the firmware type. Instead, it directly uses the firmware type file selected in the upgrade package to perform the upgrade.',
        SelectEffectiveModeItem1: 'Auto Restart After Upgrade',
        SelectEffectiveModeItem2: 'Manual Restart After Upgrade',
        SelectEffectiveModeTips: 'Automatic restart may cause service interruption. Manual restart requires you to manually restart the server after the upgrade for the upgrade to take effect.',
        clearFirmTask: 'No failed task can be  cleared ',
        type_firm: 'Type',
        size_firm: 'Size',
        liveMode_firm: 'Activation Mode',
        fileName_firm: 'File Name',
        package_firm: 'Firmware And Driver Package:',
        packageList_firm: 'Firmware And Driver List:',
        driver_firm: 'Driver:',
        outBand_firm: 'Outband:',
        inBand_firm: 'Inband:',
        detail_soft: 'Details',
        dn: "DN",
        deviceTaskResult: 'Result',
        currentVersion: 'Current Version',
        upgradeVersion: 'Upgrade Version',
        firmwareProgress: 'Progress',
        details: 'Details',
        getDNInfoErrorTips: 'Failed to obtain device upgrade details',
        getDNInfoTips: 'Getting the upgrade details for the device',
        serverName: 'Name',
        deivceFirmwareTips: 'If the device firmware or driver does not have a matching upgrade package, the firmware information is not displayed on the task upgrade list.',
        selectFirmwareTips: 'Please select firmware with the same upgrade mode.',
        highRisk: 'High Risk',
        highRiskTips1: 'Automatic restart may cause service interruption. Manual restart requires you to manually restart the server after the update for the update to take effect.',
        agreeTip: 'I have read the warning message carefully.'
    },
    task: {
        taskName: "Name",
        taskProgress: "Progress",
        taskStatus: "Status",
        createTime: "Created At",
        taskIdenty: 'Task Identy',
        dn: "DN",
        name: 'Name',
        servertype: 'Type',
        delSuccessMsg: 'Delete successfully',
        detail_soft: 'Details',
        dialogTitle: 'Details About Deployed Devices',
        deviceResult: 'Deployment Result',
        deviceProgress: 'Progress',
        errorDetail: 'Details',
        confirmDelete: 'Are you sure to delete?',
        getDeviceNameErrorTips: 'Failed to get device name'

    },
    software: {
        name: 'Name',
        task: 'Task',
        taskState: 'Task State',
        synchronousState: 'Progress',
        handle: 'Operation',
        add: 'Add',
        delete: 'Delete',
        refresh: 'Refresh',
        desc: 'Description',
        detail: 'Type',
        softName: 'Name:',
        softDesc: 'Description:',
        softType: 'Type:',
        softVersion: 'Version:',
        softLanguage: 'Language:',
        sourceName: 'Image File Name:',
        sftpPort: 'SFTP Port:',
        sftipIP: 'SFTP IP:',
        sftipIPName: 'SFTP Name:',
        sftipIPpwd: 'SFTP Password:',
        softwareList: 'Software List',
        delSuccessMsg: 'Delete successfully',
        clearSuccess: 'Delete successfully',
        beforeDeleteTips: 'Are you sure to delete?',
        beforDeleteAlert: 'Are you sure to delete?',
        deletingTips: 'Deleting',
        addSoftware: 'Add Software Source',
        nameNull: 'Name cannot be null',
        sourceNameNull: 'SourceName cannot be null',
        SFTPPortNull: 'SFTP Port cannot be null',
        SFTPIPNull: 'SFTP IP cannot be null',
        SFTPNameNull: 'SFTP name cannot be null',
        SFTPPwdNull: ' SFTP password cannot be null',
        templateNameErrorMsg: 'The name contains 6 to 32 characters\, which can include letters\, digits\, hyphens (-)\, and underscores (_)\.',
        passWordErrorMsg: 'The password must contain 0 to 64 characters', //, including at least any two types of the following: uppercase letters, lowercase letters, digits, and special characters including `~!@$%^&*()-_=+|[{}];:\'"",<.>/?
        eSightnull: 'Mandatory field',
        addSuccessMsg: 'Add successfully',
        esightHost: 'eSight Host',
        time: 'Created At',
        created: 'Uploading',
        finished: 'Done',
        syncFailed: 'Sync failed',
        hwFailed: 'Sync eSight failed',
        inputName: 'Name:',
        sftpPortError: 'SFTP Port only can between 0 and 65535',
        sftpIPError: 'The first byte of the IP address must be an integer ranging from 1 to 223 except 127. The other three bytes must be integers ranging from 0 to 255\, and the last byte cannot be 0.',
        sftpNameError: 'The name contains 0 to 64 characters.',
        sourceNameError: ' The sourceName only can include letters, digits, hyphens (-), underscores (_), and periods (.).',
        clearTask: 'No failed task can be  cleared ',
        detail_soft: 'Details'
    },
    CNA_template: {
        templateType: 'Configuration Type',
        templateName: 'Name',
        templateDesc: 'Description',
        templateMode: 'Model',
        slot: 'Slot',
        mfstatus: 'MF Status',
        sriov: 'SRIOV',
        confirm: 'Confirm',
        cancel: 'Cancel',
        templateNameErrorMsg: 'The name contains 6 to 32 characters, which can include letters, digits, hyphens (-)and underscores (_).',
        nameNull: 'Name cannot be null',
        deleteTips: 'Are you sure to close this window?',
        eSightnull: 'Mandatory field',
        adapterModelNull: 'AdapterModel cannot be null',
        pfType: 'PF Type',
        high: 'Advanced',
        pxeProp: 'PXE Properties',
        sanProp: 'SAN Properties',
        pfProp: 'PF Properties',
        miniBand: 'Min. Bandwidth Ratio(1~100%)',
        maxBand: 'Max. Bandwidth Ratio(1~100%)',
        pleaseSelect: 'Please select...',
        mfTips: 'The MF function supports multi-channel PF. After this function is enabled, a physical port is divided into four logical channels. The logical channels can be configured as network ports, Fibre Channel ports, or iSCSI ports based on site requirements.',
        pfTips: 'The PF working mode is as follows:The NIC is used to transmit Ethernet service data.The FCoE is used to access the FC SAN.The iSCSI is used to access the IP SAN.',
        intTips: 'Please enter an integer between 2~4094',
        bandTips: 'Please enter an integer between 1~100',
        createSucc: 'Add successfully',
        priority: 'Priority',
        pfVlanID: 'PF VLAN ID(2~4094)',
        inputAll: "Please enter an integer ranging from 0 to 4094.",
        inputLittle: "Please enter an integer ranging from 0 to 7.",
        ipCheck: "The first byte of the IP address must be an integer ranging from 1 to 223 except 127. The other three bytes must be integers ranging from 0 to 255\, and the last byte cannot be 0.",
        childCode: "You entered an invalid subnet mask. Please enter a valid mask.",
        inputIQN: "The value is in the format of iqn.xx:MAC address. It can contain 11 to 223 characters\, including digits (0-9)\, lowercase letters (a-z)\, uppercase letters (A-Z)\, dots (.)\, colons (:)\, and hyphens (-). For example\, iqn.1990-08.com.huawei:20-14-03-10-14-30.",
        input31: "Please enter 0~31 characters.",
        input255: "Please enter 1~255 characters.",
        input16: "Please enter 12~16 characters.",
        input65535: "Please enter an integer ranging from 1024 to 65535.",
        input3600: "Please enter an integer ranging from 0 to 3600.",
        input223: "Enter a string of 11 to 223 characters.",
        notSame: 'The minimum bandwidth ratio of all PF of {0} must be 100%.',
        error: 'Error',
        notEquil: 'The PF VLAN ID of {0}-{1} and {2}-{3} cannot be the same.',
        sameStart: "F status and SRIOV can not enable together.",
        pf1TypeSame: 'The PF type of Port0-PF1 and Port1-PF1 must be the same.',
        mz512PF1TypeSame: 'The PF type of {0}-PF1 and {1}-PF1 must be the same.',
        highBtnCon: 'Please complete PF1 the {0} of advanced configuration.',
        templateDescTips: 'The templateDesc must be between 0 and 128 characters。'
    },
    deviceInfo: {
        name: "Name",
        serverModel: "Server Model",
        servertype: "Device Type",
        lastUpdateTime: "Last Update Time",
        description: "Description",
        onlineDeviceStatus: "Online Device Status",
        cpu: "CPU",
        memory: "Memory",
        disk: "Hard Disk",
        psu: "PSU",
        fan: "Fan",
        normal: "Normal",
        offline: "Offline",
        fault: "Faulty",
        unknown: "Unknown",
        motherboard: "Main Board",
        productSN: "SN",
        sn: "Serial Number",
        manufacture: "Manufacturer",
        healthState: "Health Status",
        manuTime: "Board Manufacture Date",
        frequency: "Frequency",
        model: "Model",
        capacity: "Capacity",
        location: "Slot ID",
        inputPower: "Input Power(W)",
        inputMode: "Current Input Mode",
        version: "Version",
        rotate: "Rotational Speed(RPM)",
        rotatePercent: "Rotational Speed Percenttage(%)",
        installationStatus: "Installation Status",
        ratedPower: "Rated Power(W)",
        protocol: "Protocol",
        essentialInformation: "Essential Information",
        viewDeviceStatus: "View",
        partNumber: "Board Part Number",
        auto: "Auto",
        acInput: "AC",
        dcInput: "DC",
        acInputDcInput: "AC/DC",
        installed: "Installed",
        notInstalled: "Not Installed",
        assetTag: "Asset Tag",
        raid: 'RAID',
        raidType: "Type",
        interfaceType: "Interface Type",
        bbuType: "BBU Type",
        PCIE: 'PCIe',
        pcieSsdCardLifeLeft: 'PCIe SSD Card LifeLeft(%)',
        NetworkCard: 'Network Card',
        macAdress: 'Mac Adress',
        netWorkCardName: 'Network Card Name',
        controlModel: "Control Model",
        manual: "Manual",
        prompt: 'Prompt',
        critical: 'Critical',
        Mezz: 'Mezz',
        locationInfo: 'Location Info',
        productSn: 'SN',
        status: 'Status',
        mode: 'Model',
        uuid: 'UUID',
        installedComponentHealthState: 'Installed Component Health State',
        blade: 'Blade',
        NoRecord: 'No Records Found',
        mac: 'MAC',
        ethMac: "Ethernet Port MAC Address",
        ibMac: "InfiniBand Port MAC Address",
        fcWWPN: "FC Port WWPN",
        cpuHealthState: "CPU HealthState",
        memoryHealthState: "Memory HealthState",
        highdensityServer: "High-density Server",
        bladeServer: "Blade Server",
        deviceModel: "Device Model",
        switchBoard: "Switch Board",
        rackServer: 'Rack Server'
    }

}