# Configuration Guide

## Required Configuration Changes

### 1. MongoDB Configuration

**File**: `src/main/resources/application.properties`

```properties
# Local MongoDB (default)
spring.data.mongodb.uri=mongodb://localhost:27017/ethreader

# Remote MongoDB
spring.data.mongodb.uri=mongodb://username:password@host:port/database

# MongoDB Atlas
spring.data.mongodb.uri=mongodb+srv://username:password@cluster.mongodb.net/database
```

### 2. Web3j RPC Endpoint (REQUIRED)

**File**: `src/main/resources/application.properties`

You must replace `YOUR_PROJECT_ID` or `YOUR_API_KEY` with your actual credentials:

**Option 1: Infura**
1. Go to https://infura.io
2. Create an account and project
3. Get your Project ID
4. Update the config:
```properties
web3j.rpc.url=https://sepolia.infura.io/v3/YOUR_PROJECT_ID
```

**Option 2: Alchemy**
1. Go to https://www.alchemy.com
2. Create an account and app
3. Get your API Key
4. Update the config:
```properties
web3j.rpc.url=https://eth-sepolia.g.alchemy.com/v2/YOUR_API_KEY
```

### 3. JWT Secret (REQUIRED for Production)

**File**: `src/main/resources/application.properties`

Generate a strong secret key (at least 256 bits):

```properties
jwt.secret=your-strong-secret-key-minimum-256-bits-long
```

**How to generate:**
- Use a password generator
- Or use: `openssl rand -base64 32`

### 4. Encryption Key (REQUIRED for Production)

**File**: `src/main/resources/application.properties`

Must be exactly 32 characters for AES-256:

```properties
encryption.key=Exactly32CharactersLongKey123
```

**Important**: This key encrypts private keys in the database. Keep it secure!

## Optional Configuration

### Server Port
```properties
server.port=8080
```

### Blockchain Scan Interval (milliseconds)
```properties
blockchain.scan.interval=5000
```

### Starting Block Number
```properties
# Start from block 0 (scan all blocks)
blockchain.start.block=0

# Or start from latest block (only new blocks)
# Set to a high number or use current block number
```

## Quick Setup Checklist

- [ ] Install and start MongoDB
- [ ] Get Infura or Alchemy API key
- [ ] Update `web3j.rpc.url` in application.properties
- [ ] Generate and update `jwt.secret`
- [ ] Generate and update `encryption.key` (32 characters)
- [ ] Update MongoDB URI if not using local MongoDB
- [ ] Start the application

