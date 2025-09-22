// Configuración y variables globales
const API_BASE_URL = 'http://187.188.66.56:8024';
let currentErrorData = {};

// Base de datos de soluciones por código de error
const errorSolutions = {
    'ERR001': [
        {
            icon: 'fas fa-sync-alt',
            title: 'Reiniciar Scanner Biométrico',
            description: 'Desconecte y reconecte el dispositivo scanner, luego reinicie la aplicación.'
        },
        {
            icon: 'fas fa-usb',
            title: 'Verificar Conexión USB',
            description: 'Asegúrese de que el cable USB esté correctamente conectado y el dispositivo reconocido.'
        }
    ],
    'ERR002': [
        {
            icon: 'fas fa-network-wired',
            title: 'Verificar Conexión de Red',
            description: 'Compruebe la conexión a internet y el acceso al servidor de base de datos.'
        },
        {
            icon: 'fas fa-server',
            title: 'Estado del Servidor',
            description: 'El servidor principal puede estar experimentando problemas temporales.'
        }
    ],
    'SYS001': [
        {
            icon: 'fas fa-cogs',
            title: 'Error de Sistema',
            description: 'Se ha detectado un error interno del sistema. Intente reiniciar la aplicación.'
        },
        {
            icon: 'fas fa-file-alt',
            title: 'Revisar Logs',
            description: 'Consulte los archivos de registro para obtener más información sobre el error.'
        }
    ],
    'default': [
        {
            icon: 'fas fa-question-circle',
            title: 'Error Desconocido',
            description: 'Se ha producido un error no identificado. Contacte al soporte técnico.'
        },
        {
            icon: 'fas fa-download',
            title: 'Descargar Logs',
            description: 'Descargue los archivos de registro y envíelos al equipo de soporte.'
        }
    ]
};

// Inicialización cuando la página carga
document.addEventListener('DOMContentLoaded', function() {
    showLoadingOverlay();
    parseUrlParameters();
    setTimeout(() => {
        hideLoadingOverlay();
        animateContent();
    }, 2000);
});

// Función para mostrar/ocultar overlay de carga
function showLoadingOverlay() {
    document.getElementById('loadingOverlay').style.display = 'flex';
}

function hideLoadingOverlay() {
    document.getElementById('loadingOverlay').style.display = 'none';
}

// Función para animar contenido
function animateContent() {
    const cards = document.querySelectorAll('.info-card');
    cards.forEach((card, index) => {
        setTimeout(() => {
            card.style.opacity = '0';
            card.style.transform = 'translateY(20px)';
            card.style.transition = 'all 0.5s ease';
            setTimeout(() => {
                card.style.opacity = '1';
                card.style.transform = 'translateY(0)';
            }, 100);
        }, index * 200);
    });
}

// Función para parsear parámetros de URL
function parseUrlParameters() {
    const urlParams = new URLSearchParams(window.location.search);
    
    currentErrorData = {
        code: urlParams.get('code') || 'NOSESSION08141107-ERR001',
        session: urlParams.get('session') || 'N/A',
        device: urlParams.get('device') || 'N/A',
        user: urlParams.get('user') || 'N/A',
        timestamp: new Date().toLocaleString('es-ES', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit'
        })
    };
    
    updateErrorDisplay();
    updateErrorDetails();
    loadSolutions();
}

// Función para actualizar la visualización del error
function updateErrorDisplay() {
    const errorCode = document.getElementById('errorCode');
    const errorTitle = document.getElementById('errorTitle');
    const errorMessage = document.getElementById('errorMessage');
    
    errorCode.textContent = currentErrorData.code;
    
    // Determinar tipo de error y mensaje
    if (currentErrorData.code.includes('ERR')) {
        errorTitle.textContent = 'Error del Sistema Detectado';
        errorMessage.textContent = 'Se ha producido un error en el sistema biométrico que requiere atención.';
    } else if (currentErrorData.code.includes('SYS')) {
        errorTitle.textContent = 'Error de Sistema Interno';
        errorMessage.textContent = 'Error interno del sistema que está siendo procesado automáticamente.';
    } else {
        errorTitle.textContent = 'Evento del Sistema';
        errorMessage.textContent = 'Se ha registrado un evento que requiere revisión.';
    }
}

// Función para actualizar detalles del error
function updateErrorDetails() {
    document.getElementById('sessionInfo').textContent = currentErrorData.session || 'Sin sesión activa';
    document.getElementById('deviceInfo').textContent = currentErrorData.device || 'Dispositivo no identificado';
    document.getElementById('userInfo').textContent = currentErrorData.user || 'Usuario no identificado';
    document.getElementById('timestampInfo').textContent = currentErrorData.timestamp;
}

// Función para cargar soluciones basadas en el código de error
function loadSolutions() {
    const solutionsList = document.getElementById('solutionsList');
    const errorType = extractErrorType(currentErrorData.code);
    const solutions = errorSolutions[errorType] || errorSolutions['default'];
    
    solutionsList.innerHTML = '';
    
    solutions.forEach((solution, index) => {
        const solutionElement = document.createElement('div');
        solutionElement.className = 'solution-item';
        solutionElement.style.animationDelay = `${index * 0.1}s`;
        
        solutionElement.innerHTML = `
            <div class="solution-icon">
                <i class="${solution.icon}"></i>
            </div>
            <div class="solution-content">
                <h4>${solution.title}</h4>
                <p>${solution.description}</p>
            </div>
        `;
        
        solutionsList.appendChild(solutionElement);
    });
}

// Función para extraer tipo de error del código
function extractErrorType(errorCode) {
    if (errorCode.includes('-ERR001')) return 'ERR001';
    if (errorCode.includes('-ERR002')) return 'ERR002';
    if (errorCode.includes('-SYS001')) return 'SYS001';
    return 'default';
}

// Funciones para acciones rápidas
function restartApplication() {
    showNotification('Enviando comando de reinicio...', 'info');
    
    // Simular llamada a API para reiniciar aplicación
    setTimeout(() => {
        showNotification('Comando enviado exitosamente. La aplicación se reiniciará.', 'success');
    }, 2000);
}

function checkSystemStatus() {
    showNotification('Verificando estado del sistema...', 'info');
    
    // Simular verificación del sistema
    setTimeout(() => {
        updateSystemStatus();
        showNotification('Verificación completada. Ver estado abajo.', 'success');
    }, 3000);
}

function downloadLogs() {
    showNotification('Preparando descarga de logs...', 'info');
    
    // Crear un enlace de descarga simulado
    const logContent = generateLogFile();
    const blob = new Blob([logContent], { type: 'text/plain' });
    const url = window.URL.createObjectURL(blob);
    
    const a = document.createElement('a');
    a.href = url;
    a.download = `sistema-logs-${currentErrorData.code}-${Date.now()}.txt`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    window.URL.revokeObjectURL(url);
    
    showNotification('Logs descargados exitosamente.', 'success');
}

function openTicket() {
    showNotification('Abriendo formulario de ticket...', 'info');
    
    // Crear URL del ticket con información pre-completada
    const ticketUrl = `${API_BASE_URL}/support/ticket?code=${encodeURIComponent(currentErrorData.code)}&device=${encodeURIComponent(currentErrorData.device)}&user=${encodeURIComponent(currentErrorData.user)}`;
    
    window.open(ticketUrl, '_blank');
}

// Función para generar archivo de logs
function generateLogFile() {
    return `
GRUPO SANTORO - REPORTE DE LOGS DEL SISTEMA
============================================

Información del Error:
- Código: ${currentErrorData.code}
- Sesión: ${currentErrorData.session}
- Dispositivo: ${currentErrorData.device}
- Usuario: ${currentErrorData.user}
- Timestamp: ${currentErrorData.timestamp}

Información del Sistema:
- User Agent: ${navigator.userAgent}
- URL: ${window.location.href}
- Resolución: ${screen.width}x${screen.height}
- Memoria disponible: ${navigator.deviceMemory || 'No disponible'} GB

Estado de Conexión:
- Estado de red: ${navigator.onLine ? 'Conectado' : 'Desconectado'}
- Tipo de conexión: ${navigator.connection?.effectiveType || 'No disponible'}

Logs adicionales serían incluidos por el sistema backend...
    `.trim();
}

// Función para actualizar estado del sistema
function updateSystemStatus() {
    const statusItems = document.querySelectorAll('.status-item');
    
    statusItems.forEach((item, index) => {
        setTimeout(() => {
            const icon = item.querySelector('.status-icon');
            const text = item.querySelector('strong');
            
            // Simular diferentes estados
            if (index === 0) {
                icon.className = 'fas fa-server status-icon online';
                text.textContent = 'Online';
                text.style.color = 'var(--success-color)';
            } else if (index === 1) {
                icon.className = 'fas fa-database status-icon online';
                text.textContent = 'Online';
                text.style.color = 'var(--success-color)';
            } else {
                icon.className = 'fas fa-shield-alt status-icon warning';
                text.textContent = 'Requiere Atención';
                text.style.color = 'var(--warning-color)';
            }
        }, index * 500);
    });
}

// Función para mostrar notificaciones
function showNotification(message, type = 'info') {
    // Crear elemento de notificación
    const notification = document.createElement('div');
    notification.className = `notification notification-${type}`;
    notification.innerHTML = `
        <i class="fas fa-${type === 'success' ? 'check-circle' : type === 'error' ? 'exclamation-circle' : 'info-circle'}"></i>
        <span>${message}</span>
    `;
    
    // Estilos para la notificación
    notification.style.cssText = `
        position: fixed;
        top: 2rem;
        right: 2rem;
        background: ${type === 'success' ? 'var(--success-color)' : type === 'error' ? 'var(--error-color)' : 'var(--primary-color)'};
        color: white;
        padding: 1rem 1.5rem;
        border-radius: var(--radius-md);
        box-shadow: var(--shadow-lg);
        display: flex;
        align-items: center;
        gap: 0.5rem;
        z-index: 1001;
        animation: slideInRight 0.3s ease;
        max-width: 300px;
        font-weight: 500;
    `;
    
    document.body.appendChild(notification);
    
    // Remover la notificación después de 4 segundos
    setTimeout(() => {
        notification.style.animation = 'slideOutRight 0.3s ease';
        setTimeout(() => {
            if (notification.parentNode) {
                notification.parentNode.removeChild(notification);
            }
        }, 300);
    }, 4000);
}

// Agregar estilos CSS para animaciones de notificaciones
const notificationStyles = document.createElement('style');
notificationStyles.textContent = `
    @keyframes slideInRight {
        from {
            opacity: 0;
            transform: translateX(100%);
        }
        to {
            opacity: 1;
            transform: translateX(0);
        }
    }
    
    @keyframes slideOutRight {
        from {
            opacity: 1;
            transform: translateX(0);
        }
        to {
            opacity: 0;
            transform: translateX(100%);
        }
    }
`;
document.head.appendChild(notificationStyles);

// Función para manejar errores de red
window.addEventListener('error', function(e) {
    console.error('Error en la página:', e.error);
});

// Función para manejar cambios de conectividad
window.addEventListener('online', function() {
    showNotification('Conexión a internet restaurada', 'success');
});

window.addEventListener('offline', function() {
    showNotification('Conexión a internet perdida', 'error');
});
