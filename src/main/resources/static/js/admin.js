const API_BASE = '/api';

let authToken = localStorage.getItem('adminToken');
let currentWalletId = null;

// Store original data for filtering
let allUsers = [];
let allWallets = [];
let allTransactions = [];

// Initialize
document.addEventListener('DOMContentLoaded', function() {
    if (authToken) {
        showAdminSection();
        // Load data after a short delay to ensure DOM is ready
        setTimeout(() => {
            loadUsers();
            loadWallets();
        }, 100);
    } else {
        showAuthSection();
    }
});

// Authentication
function showAuthSection() {
    document.getElementById('authSection').style.display = 'block';
    document.getElementById('userInfo').style.display = 'none';
    document.getElementById('adminSection').style.display = 'none';
}

function showAdminSection() {
    document.getElementById('authSection').style.display = 'none';
    document.getElementById('userInfo').style.display = 'flex';
    document.getElementById('adminSection').style.display = 'block';
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
            localStorage.setItem('adminToken', authToken);
            document.getElementById('userName').textContent = data.username;
            showAdminSection();
            // Load data after a short delay to ensure DOM is ready
            setTimeout(() => {
                loadUsers();
                loadWallets();
            }, 100);
        } else {
            alert('Error: ' + (data.error || 'Login failed'));
        }
    } catch (error) {
        alert('Error: ' + error.message);
    }
}

function logout() {
    authToken = null;
    localStorage.removeItem('adminToken');
    showAuthSection();
}

// Tab Management
function showTab(tabName) {
    // Hide all tabs
    document.getElementById('usersTab').style.display = 'none';
    document.getElementById('walletsTab').style.display = 'none';
    document.getElementById('transactionsTab').style.display = 'none';
    
    // Remove active class from all buttons
    document.querySelectorAll('.tab-btn').forEach(btn => btn.classList.remove('active'));
    
    // Show selected tab
    if (tabName === 'users') {
        document.getElementById('usersTab').style.display = 'block';
        document.querySelectorAll('.tab-btn')[0].classList.add('active');
        loadUsers();
    } else if (tabName === 'wallets') {
        document.getElementById('walletsTab').style.display = 'block';
        document.querySelectorAll('.tab-btn')[1].classList.add('active');
        loadWallets();
    } else if (tabName === 'transactions') {
        document.getElementById('transactionsTab').style.display = 'block';
        document.querySelectorAll('.tab-btn')[2].classList.add('active');
        loadTransactions();
    }
}

// Users Management
async function loadUsers() {
    const tbody = document.getElementById('usersTableBody');
    
    if (!tbody) {
        console.error('usersTableBody not found');
        return;
    }
    
    try {
        const response = await fetch(`${API_BASE}/admin/users`, {
            headers: {
                'Authorization': `Bearer ${authToken}`
            }
        });

        if (!response.ok) {
            let errorMessage = 'Failed to load users';
            try {
                const errorData = await response.json();
                errorMessage = errorData.error || errorMessage;
            } catch (e) {
                errorMessage = `HTTP ${response.status}: ${response.statusText}`;
            }
            
            if (response.status === 403) {
                tbody.innerHTML = '<tr><td colspan="6" style="text-align: center; color: red;">Access denied. Admin role required.</td></tr>';
                alert('Access denied. Admin role required.');
                logout();
                return;
            } else {
                tbody.innerHTML = `<tr><td colspan="6" style="text-align: center; color: red;">Error: ${errorMessage}</td></tr>`;
                console.error('Error loading users:', errorMessage);
                return;
            }
        }

        const data = await response.json();
        allUsers = data; // Store for filtering
        displayUsers(data);
    } catch (error) {
        tbody.innerHTML = `<tr><td colspan="6" style="text-align: center; color: red;">Error: ${error.message || 'Failed to load users'}</td></tr>`;
        console.error('Error loading users:', error);
    }
}

function displayUsers(users) {
    const tbody = document.getElementById('usersTableBody');
    
    if (users.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" style="text-align: center;">No users found</td></tr>';
        return;
    }

    tbody.innerHTML = users.map(user => {
        const roleClass = user.role === 'ADMIN' ? 'admin' : 'user';
        return `
            <tr>
                <td>${user.id.substring(0, 8)}...</td>
                <td>${user.username}</td>
                <td>${user.email}</td>
                <td><span class="badge ${roleClass}">${user.role}</span></td>
                <td>${user.walletCount}</td>
                <td>${new Date(user.createdAt).toLocaleString()}</td>
            </tr>
        `;
    }).join('');
}

// Wallets Management
async function loadWallets() {
    const tbody = document.getElementById('walletsTableBody');
    
    if (!tbody) {
        console.error('walletsTableBody not found');
        return;
    }
    
    try {
        const response = await fetch(`${API_BASE}/admin/wallets`, {
            headers: {
                'Authorization': `Bearer ${authToken}`
            }
        });

        if (!response.ok) {
            let errorMessage = 'Failed to load wallets';
            try {
                const errorData = await response.json();
                errorMessage = errorData.error || errorMessage;
            } catch (e) {
                errorMessage = `HTTP ${response.status}: ${response.statusText}`;
            }
            
            if (response.status === 403) {
                tbody.innerHTML = '<tr><td colspan="7" style="text-align: center; color: red;">Access denied. Admin role required.</td></tr>';
                alert('Access denied. Admin role required.');
                logout();
                return;
            } else {
                tbody.innerHTML = `<tr><td colspan="7" style="text-align: center; color: red;">Error: ${errorMessage}</td></tr>`;
                console.error('Error loading wallets:', errorMessage);
                return;
            }
        }

        const data = await response.json();
        allWallets = data; // Store for filtering
        displayWallets(data);
    } catch (error) {
        tbody.innerHTML = `<tr><td colspan="7" style="text-align: center; color: red;">Error: ${error.message || 'Failed to load wallets'}</td></tr>`;
        console.error('Error loading wallets:', error);
    }
}

function displayWallets(wallets) {
    const tbody = document.getElementById('walletsTableBody');
    
    if (wallets.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" style="text-align: center;">No wallets found</td></tr>';
        return;
    }

    tbody.innerHTML = wallets.map(wallet => {
        const activeClass = wallet.active ? 'active' : 'inactive';
        const balance = formatBalance(wallet.balance, wallet.tokenAddress);
        const token = wallet.tokenAddress ? 'Token' : 'ETH';
        
        return `
            <tr>
                <td>${wallet.address.substring(0, 20)}...</td>
                <td>${wallet.username || 'Unknown'}</td>
                <td>${wallet.chain}</td>
                <td>${token}</td>
                <td>${balance}</td>
                <td><span class="badge ${activeClass}">${wallet.active ? 'Active' : 'Inactive'}</span></td>
                <td>
                    <button class="action-btn" onclick="viewWallet('${wallet.id}')">View</button>
                    <button class="action-btn" onclick="openTransactionModal('${wallet.id}', '${wallet.address}')">Send</button>
                </td>
            </tr>
        `;
    }).join('');
}

function formatBalance(balance, tokenAddress) {
    if (!balance) return '0';
    
    if (!tokenAddress) {
        // ETH balance
        const ethAmount = parseFloat(balance) / 1e18;
        return `${ethAmount.toFixed(6)} ETH`;
    } else {
        // Token balance (assuming 18 decimals)
        const tokenAmount = parseFloat(balance) / 1e18;
        return `${tokenAmount.toFixed(6)} Tokens`;
    }
}

// Wallet Details Modal
async function viewWallet(walletId) {
    try {
        const response = await fetch(`${API_BASE}/admin/wallets/${walletId}`, {
            headers: {
                'Authorization': `Bearer ${authToken}`
            }
        });

        const data = await response.json();
        
        if (response.ok) {
            displayWalletDetails(data);
            document.getElementById('walletModal').style.display = 'block';
        } else {
            alert('Error: ' + (data.error || 'Failed to load wallet details'));
        }
    } catch (error) {
        alert('Error: ' + error.message);
    }
}

function displayWalletDetails(wallet) {
    const detailsDiv = document.getElementById('walletDetails');
    const balance = formatBalance(wallet.balance, wallet.tokenAddress);
    const token = wallet.tokenAddress ? 'Token' : 'ETH';
    
    detailsDiv.innerHTML = `
        <div class="wallet-details">
            <div class="wallet-details-item">
                <strong>Wallet ID:</strong>
                <span class="value">${wallet.id}</span>
            </div>
            <div class="wallet-details-item">
                <strong>User:</strong>
                <span class="value">${wallet.username || 'Unknown'}</span>
            </div>
            <div class="wallet-details-item">
                <strong>Address (Public Key):</strong>
                <span class="value">${wallet.address}</span>
                <button class="copy-btn" onclick="copyToClipboard('${wallet.address}')">Copy</button>
            </div>
            <div class="wallet-details-item">
                <strong>Private Key:</strong>
                <div class="private-key-container">
                    <span class="value private-key-masked" id="privateKeyMasked">${'*'.repeat(64)}</span>
                    <span class="value" id="privateKeyRevealed" style="display: none;">${wallet.privateKey}</span>
                    <button class="reveal-btn" onclick="togglePrivateKey()">Reveal</button>
                    <button class="copy-btn" onclick="copyToClipboard('${wallet.privateKey}')" id="copyPrivateKeyBtn" style="display: none;">Copy</button>
                </div>
            </div>
            <div class="wallet-details-item">
                <strong>Chain:</strong>
                <span class="value">${wallet.chain}</span>
            </div>
            <div class="wallet-details-item">
                <strong>Token:</strong>
                <span class="value">${token}</span>
            </div>
            <div class="wallet-details-item">
                <strong>Balance:</strong>
                <span class="value">${balance}</span>
            </div>
            <div class="wallet-details-item">
                <strong>Active:</strong>
                <span class="value">${wallet.active ? 'Yes' : 'No'}</span>
            </div>
            <div class="wallet-details-item">
                <strong>Created At:</strong>
                <span class="value">${new Date(wallet.createdAt).toLocaleString()}</span>
            </div>
        </div>
    `;
}

function togglePrivateKey() {
    const masked = document.getElementById('privateKeyMasked');
    const revealed = document.getElementById('privateKeyRevealed');
    const copyBtn = document.getElementById('copyPrivateKeyBtn');
    const revealBtn = document.querySelector('.reveal-btn');
    
    if (masked.style.display !== 'none') {
        masked.style.display = 'none';
        revealed.style.display = 'inline';
        copyBtn.style.display = 'inline-block';
        revealBtn.textContent = 'Hide';
    } else {
        masked.style.display = 'inline';
        revealed.style.display = 'none';
        copyBtn.style.display = 'none';
        revealBtn.textContent = 'Reveal';
    }
}

function closeWalletModal() {
    document.getElementById('walletModal').style.display = 'none';
}

// Transaction Modal
function openTransactionModal(walletId, fromAddress) {
    currentWalletId = walletId;
    document.getElementById('fromAddress').value = fromAddress;
    document.getElementById('toAddress').value = '';
    document.getElementById('tokenSelect').value = '';
    document.getElementById('amount').value = '';
    document.getElementById('transactionModal').style.display = 'block';
}

function closeTransactionModal() {
    document.getElementById('transactionModal').style.display = 'none';
    currentWalletId = null;
}

async function sendTransaction() {
    const toAddress = document.getElementById('toAddress').value;
    const tokenAddress = document.getElementById('tokenSelect').value;
    const amountInput = document.getElementById('amount').value;
    
    if (!toAddress || !amountInput) {
        alert('Please fill in all required fields');
        return;
    }
    
    // Convert amount to wei (assuming 18 decimals for both ETH and tokens)
    const amount = BigInt(Math.floor(parseFloat(amountInput) * 1e18));
    
    try {
        const response = await fetch(`${API_BASE}/admin/wallets/${currentWalletId}/send`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${authToken}`
            },
            body: JSON.stringify({
                toAddress: toAddress,
                amount: amount.toString(),
                tokenAddress: tokenAddress || null
            })
        });

        const data = await response.json();
        
        if (response.ok) {
            alert(`Transaction sent successfully!\nTx Hash: ${data.transactionHash}\n\nView on Etherscan: ${data.etherscanUrl}`);
            closeTransactionModal();
            loadWallets(); // Refresh wallets to update balances
        } else {
            alert('Error: ' + (data.message || data.error || 'Transaction failed'));
        }
    } catch (error) {
        alert('Error: ' + error.message);
    }
}

// Utility Functions
function copyToClipboard(text) {
    navigator.clipboard.writeText(text).then(() => {
        alert('Copied to clipboard!');
    }).catch(err => {
        // Fallback for older browsers
        const textarea = document.createElement('textarea');
        textarea.value = text;
        document.body.appendChild(textarea);
        textarea.select();
        document.execCommand('copy');
        document.body.removeChild(textarea);
        alert('Copied to clipboard!');
    });
}

// Transaction History Management
async function loadTransactions() {
    const tbody = document.getElementById('transactionsTableBody');
    
    if (!tbody) {
        console.error('transactionsTableBody not found');
        return;
    }
    
    try {
        const response = await fetch(`${API_BASE}/admin/transactions`, {
            headers: {
                'Authorization': `Bearer ${authToken}`
            }
        });

        if (!response.ok) {
            let errorMessage = 'Failed to load transactions';
            try {
                const errorData = await response.json();
                errorMessage = errorData.error || errorMessage;
            } catch (e) {
                errorMessage = `HTTP ${response.status}: ${response.statusText}`;
            }
            
            if (response.status === 403) {
                tbody.innerHTML = '<tr><td colspan="11" style="text-align: center; color: red;">Access denied. Admin role required.</td></tr>';
                alert('Access denied. Admin role required.');
                logout();
                return;
            } else {
                tbody.innerHTML = `<tr><td colspan="11" style="text-align: center; color: red;">Error: ${errorMessage}</td></tr>`;
                console.error('Error loading transactions:', errorMessage);
                return;
            }
        }

        const data = await response.json();
        allTransactions = data; // Store for filtering
        displayTransactions(data);
    } catch (error) {
        tbody.innerHTML = `<tr><td colspan="11" style="text-align: center; color: red;">Error: ${error.message || 'Failed to load transactions'}</td></tr>`;
        console.error('Error loading transactions:', error);
    }
}

function displayTransactions(transactions) {
    const tbody = document.getElementById('transactionsTableBody');
    
    if (transactions.length === 0) {
        tbody.innerHTML = '<tr><td colspan="11" style="text-align: center;">No transactions found</td></tr>';
        return;
    }

    tbody.innerHTML = transactions.map(tx => {
        const statusClass = tx.status === 'CREDITED' ? 'badge active' : 
                           tx.status === 'FAILED' ? 'badge danger' : 
                           'badge inactive';
        const amount = formatTransactionAmount(tx.amount, tx.tokenAddress);
        const token = tx.tokenAddress ? 'Token' : 'ETH';
        const etherscanUrl = tx.etherscanUrl || '#';
        
        return `
            <tr>
                <td>${tx.id.substring(0, 8)}...</td>
                <td>${tx.transactionHash.substring(0, 20)}...</td>
                <td>${tx.walletAddress.substring(0, 20)}...</td>
                <td>${tx.userId ? tx.userId.substring(0, 8) + '...' : 'N/A'}</td>
                <td>${amount}</td>
                <td>${token}</td>
                <td><span class="${statusClass}">${tx.status}</span></td>
                <td>${tx.confirmations || 0}</td>
                <td>${tx.blockNumber || 'N/A'}</td>
                <td>${new Date(tx.createdAt).toLocaleString()}</td>
                <td>
                    <a href="${etherscanUrl}" target="_blank" class="action-btn">View</a>
                </td>
            </tr>
        `;
    }).join('');
}

function formatTransactionAmount(amount, tokenAddress) {
    if (!amount) return '0';
    
    if (!tokenAddress) {
        // ETH amount
        const ethAmount = parseFloat(amount) / 1e18;
        return `${ethAmount.toFixed(6)} ETH`;
    } else {
        // Token amount (assuming 18 decimals)
        const tokenAmount = parseFloat(amount) / 1e18;
        return `${tokenAmount.toFixed(6)}`;
    }
}

// Search Functions
function searchUsers() {
    const searchTerm = document.getElementById('usersSearch').value.toLowerCase().trim();
    
    if (!searchTerm) {
        displayUsers(allUsers);
        return;
    }
    
    const filtered = allUsers.filter(user => 
        user.id.toLowerCase().includes(searchTerm) ||
        user.username.toLowerCase().includes(searchTerm)
    );
    
    displayUsers(filtered);
}

function clearUsersSearch() {
    document.getElementById('usersSearch').value = '';
    displayUsers(allUsers);
}

function searchWallets() {
    const searchTerm = document.getElementById('walletsSearch').value.toLowerCase().trim();
    
    if (!searchTerm) {
        displayWallets(allWallets);
        return;
    }
    
    const filtered = allWallets.filter(wallet => 
        wallet.id.toLowerCase().includes(searchTerm) ||
        wallet.address.toLowerCase().includes(searchTerm)
    );
    
    displayWallets(filtered);
}

function clearWalletsSearch() {
    document.getElementById('walletsSearch').value = '';
    displayWallets(allWallets);
}

function searchTransactions() {
    const searchTerm = document.getElementById('transactionsSearch').value.toLowerCase().trim();
    
    if (!searchTerm) {
        displayTransactions(allTransactions);
        return;
    }
    
    const filtered = allTransactions.filter(tx => 
        tx.id.toLowerCase().includes(searchTerm) ||
        (tx.transactionHash && tx.transactionHash.toLowerCase().includes(searchTerm))
    );
    
    displayTransactions(filtered);
}

function clearTransactionsSearch() {
    document.getElementById('transactionsSearch').value = '';
    displayTransactions(allTransactions);
}

// Close modals when clicking outside
window.onclick = function(event) {
    const walletModal = document.getElementById('walletModal');
    const transactionModal = document.getElementById('transactionModal');
    
    if (event.target === walletModal) {
        closeWalletModal();
    }
    if (event.target === transactionModal) {
        closeTransactionModal();
    }
}

