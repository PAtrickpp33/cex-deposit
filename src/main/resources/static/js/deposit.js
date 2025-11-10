const API_BASE = '/api';

let authToken = localStorage.getItem('authToken');
let currentAddress = null;
let refreshInterval = null;

// Initialize
document.addEventListener('DOMContentLoaded', function() {
    if (authToken) {
        showDepositSection();
        loadCurrentAddress(); // Load current address on page load
        startAutoRefresh();
    } else {
        showAuthSection();
    }
});

// Authentication
function showLogin() {
    document.getElementById('loginForm').style.display = 'flex';
    document.getElementById('registerForm').style.display = 'none';
}

function showRegister() {
    document.getElementById('loginForm').style.display = 'none';
    document.getElementById('registerForm').style.display = 'flex';
}

function showAuthSection() {
    document.getElementById('authSection').style.display = 'block';
    document.getElementById('userInfo').style.display = 'none';
    document.getElementById('depositSection').style.display = 'none';
}

function showDepositSection() {
    document.getElementById('authSection').style.display = 'none';
    document.getElementById('userInfo').style.display = 'flex';
    document.getElementById('depositSection').style.display = 'block';
}

async function register() {
    const username = document.getElementById('regUsername').value;
    const email = document.getElementById('regEmail').value;
    const password = document.getElementById('regPassword').value;

    try {
        const response = await fetch(`${API_BASE}/auth/register`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ username, email, password })
        });

        const data = await response.json();
        
        if (response.ok) {
            alert('Registration successful! Please login.');
            showLogin();
        } else {
            alert('Error: ' + (data.error || 'Registration failed'));
        }
    } catch (error) {
        alert('Error: ' + error.message);
    }
}

async function login() {
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;

    try {
        const response = await fetch(`${API_BASE}/auth/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ username, password })
        });

        const data = await response.json();
        
        if (response.ok) {
            authToken = data.token;
            localStorage.setItem('authToken', authToken);
            document.getElementById('userName').textContent = data.username;
            showDepositSection();
            loadCurrentAddress(); // Load current address after login
            startAutoRefresh();
        } else {
            alert('Error: ' + (data.error || 'Login failed'));
        }
    } catch (error) {
        alert('Error: ' + error.message);
    }
}

function logout() {
    authToken = null;
    localStorage.removeItem('authToken');
    currentAddress = null;
    stopAutoRefresh();
    showAuthSection();
    document.getElementById('addressSection').style.display = 'none';
}

// Deposit Address
async function loadCurrentAddress() {
    const tokenSelect = document.getElementById('tokenSelect');
    const chainSelect = document.getElementById('chainSelect');
    
    // Get values, use defaults if elements exist
    const tokenAddress = tokenSelect ? tokenSelect.value : '';
    const chain = chainSelect ? chainSelect.value : 'sepolia';

    try {
        const response = await fetch(
            `${API_BASE}/deposit/current-address?chain=${chain}&tokenAddress=${tokenAddress || ''}`,
            {
                headers: {
                    'Authorization': `Bearer ${authToken}`
                }
            }
        );

        const data = await response.json();
        
        if (response.ok) {
            currentAddress = data.address;
            document.getElementById('depositAddress').value = data.address;
            document.getElementById('qrCode').src = data.qrCodeData;
            document.getElementById('addressSection').style.display = 'block';
            
            const tokenName = tokenAddress ? 'Token' : 'ETH';
            document.getElementById('tokenName').textContent = tokenName;
            
            loadPendingDeposits();
            loadDepositHistory(); // Load history when address is loaded
        } else {
            // No current address, don't show error, just hide address section
            document.getElementById('addressSection').style.display = 'none';
        }
    } catch (error) {
        // Silently fail, user can generate new address
        console.log('No current address found');
    }
}

async function getDepositAddress() {
    const tokenAddress = document.getElementById('tokenSelect').value;
    const chain = document.getElementById('chainSelect').value;

    try {
        const response = await fetch(
            `${API_BASE}/deposit/address?chain=${chain}&tokenAddress=${tokenAddress || ''}`,
            {
                headers: {
                    'Authorization': `Bearer ${authToken}`
                }
            }
        );

        const data = await response.json();
        
        if (response.ok) {
            currentAddress = data.address;
            document.getElementById('depositAddress').value = data.address;
            document.getElementById('qrCode').src = data.qrCodeData;
            document.getElementById('addressSection').style.display = 'block';
            
            const tokenName = tokenAddress ? 'Token' : 'ETH';
            document.getElementById('tokenName').textContent = tokenName;
            
            loadPendingDeposits();
            loadDepositHistory(); // Load history when address is loaded
        } else {
            alert('Error: ' + (data.error || 'Failed to get deposit address'));
        }
    } catch (error) {
        alert('Error: ' + error.message);
    }
}

async function refreshAddress() {
    const tokenAddress = document.getElementById('tokenSelect').value;
    const chain = document.getElementById('chainSelect').value;

    try {
        const response = await fetch(`${API_BASE}/deposit/refresh-address`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${authToken}`
            },
            body: JSON.stringify({ chain, tokenAddress: tokenAddress || null })
        });

        const data = await response.json();
        
        if (response.ok) {
            currentAddress = data.address;
            document.getElementById('depositAddress').value = data.address;
            document.getElementById('qrCode').src = data.qrCodeData;
            document.getElementById('addressSection').style.display = 'block';
            
            const tokenName = tokenAddress ? 'Token' : 'ETH';
            document.getElementById('tokenName').textContent = tokenName;
            
            alert('New address generated!');
        } else {
            alert('Error: ' + (data.error || 'Failed to refresh address'));
        }
    } catch (error) {
        alert('Error: ' + error.message);
    }
}

function copyAddress() {
    const addressInput = document.getElementById('depositAddress');
    addressInput.select();
    document.execCommand('copy');
    alert('Address copied to clipboard!');
}

// Pending Deposits
async function loadPendingDeposits() {
    try {
        const response = await fetch(`${API_BASE}/deposit/pending`, {
            headers: {
                'Authorization': `Bearer ${authToken}`
            }
        });

        const data = await response.json();
        
        if (response.ok) {
            displayDeposits(data);
        } else {
            console.error('Error loading deposits:', data.error);
        }
    } catch (error) {
        console.error('Error loading deposits:', error);
    }
}

// Deposit History
async function loadDepositHistory() {
    try {
        const response = await fetch(`${API_BASE}/deposit/history`, {
            headers: {
                'Authorization': `Bearer ${authToken}`
            }
        });

        const data = await response.json();
        
        if (response.ok) {
            displayHistoryDeposits(data);
        } else {
            console.error('Error loading deposit history:', data.error);
        }
    } catch (error) {
        console.error('Error loading deposit history:', error);
    }
}

function displayDeposits(deposits) {
    const depositsList = document.getElementById('depositsList');
    
    if (deposits.length === 0) {
        depositsList.innerHTML = '<p class="no-deposits">No pending deposits</p>';
        return;
    }

    depositsList.innerHTML = deposits.map(deposit => {
        const amount = formatAmount(deposit.amount, deposit.tokenAddress);
        const status = deposit.status.toLowerCase();
        const confirmations = deposit.confirmations || 0;
        const etherscanUrl = deposit.etherscanUrl || '#';
        
        return `
            <div class="deposit-item">
                <div class="deposit-item-header">
                    <strong>${amount}</strong>
                    <span class="status ${status}">${status.toUpperCase()}</span>
                </div>
                <div class="deposit-item-details">
                    <div><strong>Tx Hash:</strong> ${deposit.transactionHash.substring(0, 20)}...</div>
                    <div><strong>Confirmations:</strong> ${confirmations}/12</div>
                    <div><strong>Block:</strong> ${deposit.blockNumber}</div>
                    <div><strong>Time:</strong> ${new Date(deposit.createdAt).toLocaleString()}</div>
                </div>
                <div class="deposit-item-actions">
                    <a href="${etherscanUrl}" target="_blank" class="etherscan-btn">View on Etherscan</a>
                </div>
            </div>
        `;
    }).join('');
}

function displayHistoryDeposits(deposits) {
    const historyList = document.getElementById('historyList');
    
    // Filter only completed deposits (CREDITED or FAILED)
    const completedDeposits = deposits.filter(deposit => 
        deposit.status === 'CREDITED' || deposit.status === 'FAILED'
    );
    
    if (completedDeposits.length === 0) {
        historyList.innerHTML = '<p class="no-deposits">No deposit history</p>';
        return;
    }

    historyList.innerHTML = completedDeposits.map(deposit => {
        const amount = formatAmount(deposit.amount, deposit.tokenAddress);
        const status = deposit.status.toLowerCase();
        const confirmations = deposit.confirmations || 0;
        const etherscanUrl = deposit.etherscanUrl || '#';
        const processedTime = deposit.processedAt ? new Date(deposit.processedAt).toLocaleString() : 'N/A';
        
        return `
            <div class="deposit-item history-item">
                <div class="deposit-item-header">
                    <strong>${amount}</strong>
                    <span class="status ${status}">${status.toUpperCase()}</span>
                </div>
                <div class="deposit-item-details">
                    <div><strong>Tx Hash:</strong> ${deposit.transactionHash.substring(0, 20)}...</div>
                    <div><strong>Confirmations:</strong> ${confirmations}/12</div>
                    <div><strong>Block:</strong> ${deposit.blockNumber}</div>
                    <div><strong>Received:</strong> ${new Date(deposit.createdAt).toLocaleString()}</div>
                    <div><strong>Completed:</strong> ${processedTime}</div>
                </div>
                <div class="deposit-item-actions">
                    <a href="${etherscanUrl}" target="_blank" class="etherscan-btn">View on Etherscan</a>
                </div>
            </div>
        `;
    }).join('');
}

function formatAmount(amount, tokenAddress) {
    // Convert from wei to ETH (divide by 10^18)
    if (!tokenAddress) {
        const ethAmount = parseFloat(amount) / 1e18;
        return `${ethAmount.toFixed(6)} ETH`;
    } else {
        // For tokens, you might want to fetch decimals from contract
        // For now, just show the raw amount
        return `${amount} Tokens`;
    }
}

// Auto-refresh
function startAutoRefresh() {
    // Refresh deposits every 5 seconds
    refreshInterval = setInterval(() => {
        if (authToken && currentAddress) {
            loadPendingDeposits();
            loadDepositHistory(); // Also refresh history
        }
    }, 5000);
    
    // Initial load
    if (currentAddress) {
        loadPendingDeposits();
        loadDepositHistory();
    }
}

function stopAutoRefresh() {
    if (refreshInterval) {
        clearInterval(refreshInterval);
        refreshInterval = null;
    }
}

