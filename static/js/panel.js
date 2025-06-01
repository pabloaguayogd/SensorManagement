// Datos iniciales
    let groups = [];
    let devices = {};

    let latest_sensors_data = {};
    let latest_alarms_activations = {};

    let currentGroupId = null;

    const user_id = sessionStorage["user_id"];

    // Elementos del DOM
    const groupsContainer = document.getElementById('groupsContainer');
    const addGroupBtn = document.getElementById('addGroupBtn');
    const groupModal = document.getElementById('groupModal');
    const deviceModal = document.getElementById('deviceModal');
    const groupForm = document.getElementById('groupForm');
    const deviceForm = document.getElementById('deviceForm');
    const saveGroupBtn = document.getElementById('saveGroupBtn');
    const saveDeviceBtn = document.getElementById('saveDeviceBtn');
    const cancelGroupBtn = document.getElementById('cancelGroupBtn');
    const cancelDeviceBtn = document.getElementById('cancelDeviceBtn');
    const modalCloseButtons = document.querySelectorAll('.modal-close');

    const tabla_alarmas = document.getElementById('tabla-alarmas');
    const tabla_sensores = document.getElementById('tabla-sensor');

    async function getLatestAlarmActivations(){

        tabla_alarmas.innerHTML = ""

        for(let group of groups){
            let resp_alarmact = await axios.get(`http://localhost:8081/api/control/group/${group.group_id}/alarmStates/latest`);
            let alarmStates = resp_alarmact.data.data;
            console.log(resp_alarmact);
            for(let [key, alarmAct] of Object.entries(alarmStates)){
                genAlarmaEntry(alarmAct);
            }
        }


    }

    function genAlarmaEntry(alarmData){
        const groupElement = document.createElement('tr');
                    groupElement.innerHTML = `
                        <td>${alarmData.name}</td>
                        <td>${alarmData.alarm_id}</td>
                        <td>${alarmData.timestamp}</td>
                    `;

                    tabla_alarmas.appendChild(groupElement);
    }

    async function getLatestSensorValues(){

        tabla_sensores.innerHTML = "";

        for(let group of groups){
            let resp_sensorvals = await axios.get(`http://localhost:8081/api/control/group/${group.group_id}/sensorValues/latest`);

            for(let [key, sensVal] of Object.entries(resp_sensorvals.data.data)){
                genSensorEntry(sensVal);
            }
        }
    }

    function genSensorEntry(sensorData){
        const groupElement = document.createElement('tr');
                            groupElement.innerHTML = `
                                <td>${sensorData.name}</td>
                                <td>${sensorData.sensor_id}</td>
                                <td>${sensorData.value}</td>
                                <td>${sensorData.timestamp}</td>
                            `;

                            tabla_sensores.appendChild(groupElement);
    }


    // Funciones para mostrar/ocultar modales
    function openModal(modal) {
        modal.style.display = 'flex';
        document.body.style.overflow = 'hidden';
    }

    function closeModal(modal) {
        modal.style.display = 'none';
        document.body.style.overflow = 'auto';
    }

    // Renderizar grupos

    async function getGroups(){

     groups = [];
     let resp = await axios.get("http://localhost:8080/api/users/groups/"+user_id);
     if(resp.status == 404){
        return;
     }

     console.log(resp);
     for(let group of resp.data){

        groups.push(group);
     }

    }

    async function getDevices(group_id){

        devices = {};

        devices[group_id] = [];

        let resp_sensors = await axios.get(`http://localhost:8080/api/groups/${group_id}/sensors`);
         for(let sensor of resp_sensors.data){
            console.log(sensor);
            devices[group_id].push(sensor);
         }

         let resp_alarms = await axios.get(`http://localhost:8080/api/groups/${group_id}/alarms`);
         for(let alarm of resp_alarms.data){
            console.log(alarm);
            devices[group_id].push(alarm);
         }

    }

    async function renderGroups() {
        groupsContainer.innerHTML = '';

        try{
            await getGroups();

            for(let group of groups){
                await getDevices(group.group_id);
            }
        }catch(e){
            console.log(e);
        }




        groups.forEach(group => {
            const groupElement = document.createElement('div');
            groupElement.className = 'group-card';
            groupElement.innerHTML = `
                <div class="group-header">
                    <div class="group-title">${group.name}</div>
                    <div class="group-title">ID: ${group.group_id}</div>
                    <div class="group-actions">
                        <button class="group-action-btn add-device-btn" data-group="${group.id}">
                            <i class="fas fa-plus"></i>
                        </button>
                    </div>
                </div>
                <div class="devices-container">
                    ${devices[group.group_id].map(device => `
                        <div class="device-card">
                            <div class="device-icon ${device.type === 'camera' ? 'camera' : device.type === 'lock' ? 'lock' : 'sensor'}">
                                <i class="fas fa-microchip"></i>
                            </div>
                            <div class="device-name">${device.name}</div>
                            <div class="device-status ${device.status}">${device.status === 'online' ? 'En línea' : 'Desconectado'}</div>
                        </div>
                    `).join('')}
                </div>
            `;
            groupsContainer.appendChild(groupElement);
        });

        // Asignar eventos a los botones
        document.querySelectorAll('.add-device-btn').forEach(btn => {
            btn.addEventListener('click', () => {
                currentGroupId = btn.getAttribute('data-group');
                openModal(deviceModal);
            });
        });
    }

    // Agregar nuevo grupo
    async function addGroup() {
        const name = document.getElementById('groupName').value;
        const home_id = document.getElementById('groupHomeId').value;

        console.log();

        const newGroup = {
            name: name,
            mqtt_channel: name+home_id,
            home_id: home_id,
            suppressed: false
        };

        await axios.post("http://localhost:8080/api/groups", newGroup);

        renderGroups();
        closeModal(groupModal);
        groupForm.reset();
    }

    // Agregar nuevo dispositivo
    async function addDevice() {
        const name = document.getElementById('deviceName').value;
        const type = document.getElementById('deviceType').value;
        const group_id = document.getElementById('deviceGroupId').value;

        let resp = await axios.post("http://localhost:8080/api/devices", {name: name, group_id: group_id});




        if(type == "alarm"){
            await axios.post("http://localhost:8080/api/alarms", {name: name, type:"NoDef", device_id: resp.data.device_id});
        }else if(type == "sensor"){
            await axios.post("http://localhost:8080/api/sensors", {name: name, type:"NoDef", device_id: resp.data.device_id});
        }

        renderGroups();

        closeModal(deviceModal);
        deviceForm.reset();
    }

    // Event Listeners
    addGroupBtn.addEventListener('click', () => {
        openModal(groupModal);
    });

    saveGroupBtn.addEventListener('click', addGroup);
    saveDeviceBtn.addEventListener('click', addDevice);

    cancelGroupBtn.addEventListener('click', () => {
        closeModal(groupModal);
        groupForm.reset();
    });

    cancelDeviceBtn.addEventListener('click', () => {
        closeModal(deviceModal);
        deviceForm.reset();
    });

    modalCloseButtons.forEach(btn => {
        btn.addEventListener('click', () => {
            const modal = btn.closest('.modal');
            closeModal(modal);

            if (modal === groupModal) groupForm.reset();
            if (modal === deviceModal) deviceForm.reset();
        });
    });

    // Cerrar modal al hacer clic fuera del contenido
    window.addEventListener('click', (e) => {
        if (e.target === groupModal) {
            closeModal(groupModal);
            groupForm.reset();
        }
        if (e.target === deviceModal) {
            closeModal(deviceModal);
            deviceForm.reset();
        }
    });

    // Renderizar grupos iniciales
    renderGroups();

    document.addEventListener("DOMContentLoaded", function(event) {
         setInterval(async function(){
            await getLatestSensorValues();
            await getLatestAlarmActivations();
         }, 10000);


      });
