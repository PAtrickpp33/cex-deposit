# ETH Reader - Hot Wallet Management System

## 项目简介 / Project Introduction

ETH Reader 是一个基于 Spring Boot 和 Web3j 的以太坊热钱包管理系统，提供用户注册、登录、热钱包生成、链上充值监控和管理员后台等功能。

ETH Reader is a hot wallet management system for Ethereum based on Spring Boot and Web3j, providing user registration, login, hot wallet generation, on-chain deposit monitoring, and admin dashboard features.

### 主要功能 / Key Features

- **用户管理** / **User Management**: 用户注册、登录、JWT 认证
- **热钱包生成** / **Hot Wallet Generation**: 为每个用户生成唯一的以太坊热钱包
- **链上充值监控** / **On-chain Deposit Monitoring**: 实时监控区块链上的充值交易
- **幂等处理** / **Idempotent Processing**: 使用阻塞队列确保"精确一次"入账
- **管理员后台** / **Admin Dashboard**: 完整的用户、钱包和交易管理界面
- **交易发送** / **Transaction Sending**: 管理员可以通过私钥发送交易

### 技术栈 / Technology Stack

- **后端** / **Backend**: Spring Boot 3.x, Spring Security, Spring Data MongoDB
- **区块链** / **Blockchain**: Web3j 4.9.8
- **数据库** / **Database**: MongoDB
- **认证** / **Authentication**: JWT (JSON Web Tokens)
- **加密** / **Encryption**: AES-256
- **前端** / **Frontend**: HTML, CSS, JavaScript, Thymeleaf

---

## 系统架构 / System Architecture

### 整体架构图 / Overall Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                      Frontend Layer                          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐    │
│  │ Deposit Page │  │  Admin Page   │  │   REST API   │    │
│  └──────────────┘  └──────────────┘  └──────────────┘    │
└─────────────────────────────────────────────────────────────┘
                            │
┌─────────────────────────────────────────────────────────────┐
│                    Application Layer                         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐    │
│  │   Controllers│  │    Services   │  │    Utils     │    │
│  └──────────────┘  └──────────────┘  └──────────────┘    │
└─────────────────────────────────────────────────────────────┘
                            │
┌─────────────────────────────────────────────────────────────┐
│                      Data Layer                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐    │
│  │  MongoDB     │  │  Repositories│  │   Models     │    │
│  └──────────────┘  └──────────────┘  └──────────────┘    │
└─────────────────────────────────────────────────────────────┘
                            │
┌─────────────────────────────────────────────────────────────┐
│                   Blockchain Layer                           │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐    │
│  │   Web3j      │  │  RPC Node    │  │  Etherscan   │    │
│  └──────────────┘  └──────────────┘  └──────────────┘    │
└─────────────────────────────────────────────────────────────┘
```

### 核心模块 / Core Modules

#### 1. 用户认证模块 / Authentication Module

**文件位置** / **File Location**: 
- `controller/AuthController.java`
- `service/UserService.java`
- `util/JwtUtil.java`
- `util/JwtAuthenticationFilter.java`

**功能** / **Functionality**:
- 用户注册和登录
- JWT Token 生成和验证
- 密码加密存储（BCrypt）
- 角色管理（ADMIN/USER）

#### 2. 钱包管理模块 / Wallet Management Module

**文件位置** / **File Location**:
- `controller/WalletController.java`
- `service/WalletService.java`
- `model/HotWallet.java`

**功能** / **Functionality**:
- 热钱包生成（使用 Web3j）
- 私钥加密存储（AES-256）
- 钱包地址持久化
- 支持多链和多代币

#### 3. 充值监控模块 / Deposit Monitoring Module

**文件位置** / **File Location**:
- `service/BlockchainService.java`
- `service/DepositMonitorService.java`
- `service/DepositProcessorService.java`
- `model/DepositTransaction.java`

**功能** / **Functionality**:
- 区块链区块扫描
- 交易检测和解析
- 使用阻塞队列进行异步处理
- 幂等性保证（防止重复入账）

**处理流程** / **Processing Flow**:
```
Blockchain → BlockchainService → DepositMonitorService 
    → BlockingQueue → DepositProcessorService → MongoDB
```

#### 4. 管理员模块 / Admin Module

**文件位置** / **File Location**:
- `controller/AdminController.java`
- `service/TransactionService.java`
- `service/AdminInitializationService.java`

**功能** / **Functionality**:
- 用户列表查看
- 钱包列表查看（包含私钥）
- 交易历史查看
- 通过私钥发送交易

#### 5. 安全模块 / Security Module

**文件位置** / **File Location**:
- `config/SecurityConfig.java`
- `util/EncryptionUtil.java`
- `util/PasswordUtil.java`

**功能** / **Functionality**:
- Spring Security 配置
- 基于角色的访问控制（RBAC）
- 私钥加密/解密
- 密码哈希

---

## 数据模型 / Data Models

### User (用户)
```java
- id: String (MongoDB ObjectId)
- username: String (唯一)
- password: String (BCrypt 哈希)
- email: String
- role: UserRole (ADMIN/USER)
- createdAt: LocalDateTime
```

### HotWallet (热钱包)
```java
- id: String
- userId: String (关联用户)
- address: String (以太坊地址)
- privateKey: String (AES-256 加密)
- chain: String (链名称，如 "sepolia")
- tokenAddress: String (代币地址，null 表示原生 ETH)
- active: boolean (是否激活)
- createdAt: LocalDateTime
```

### DepositTransaction (充值交易)
```java
- id: String
- transactionHash: String (交易哈希)
- walletAddress: String (钱包地址)
- userId: String (用户ID)
- amount: BigInteger (金额，wei 单位)
- tokenAddress: String (代币地址)
- chain: String (链名称)
- blockNumber: BigInteger
- confirmations: int (确认数)
- status: DepositStatus (PENDING/CONFIRMING/CONFIRMED/CREDITED/FAILED)
- createdAt: LocalDateTime
- processedAt: LocalDateTime
```

### DepositIdempotency (幂等性记录)
```java
- id: String
- transactionHash: String (唯一)
- processed: boolean
- processedAt: LocalDateTime
```

---

## 安装和配置 / Installation & Configuration

### 前置要求 / Prerequisites

- Java 21+
- Maven 3.6+
- MongoDB 4.4+
- 以太坊 RPC 节点访问（Infura 或 Alchemy）

### 安装步骤 / Installation Steps

1. **克隆项目** / **Clone Repository**
```bash
git clone <repository-url>
cd ethReader
```

2. **配置数据库** / **Configure Database**
   - 复制 `src/main/resources/application.properties.example` 为 `application.properties`
   - 修改 MongoDB 连接字符串

3. **生成密钥** / **Generate Keys**
   
   **使用 PowerShell (Windows)**:
   ```powershell
   # 创建 generate-keys.ps1 文件
   # 运行脚本生成 JWT secret 和 encryption key
   ```
   
   **使用 OpenSSL (Linux/Mac)**:
   ```bash
   # 生成 JWT secret (至少 32 字节)
   openssl rand -base64 32
   
   # 生成 encryption key (32 字符)
   openssl rand -base64 24 | head -c 32
   ```

4. **配置 RPC 节点** / **Configure RPC Node**
   - 在 Infura 或 Alchemy 注册账号
   - 获取 API Key
   - 在 `application.properties` 中配置 `web3j.rpc.url`

5. **运行应用** / **Run Application**
```bash
mvn spring-boot:run
```

### 配置文件说明 / Configuration File Description

**application.properties.example** 包含所有必需的配置项：

- **MongoDB**: 数据库连接字符串
- **JWT**: Token 密钥和过期时间
- **Encryption**: AES-256 加密密钥
- **Web3j**: 以太坊 RPC 节点 URL
- **Blockchain**: 确认数、扫描间隔等参数

---

## API 文档 / API Documentation

### 用户认证 / Authentication

#### 注册 / Register
```
POST /api/auth/register
Content-Type: application/json

{
  "username": "user123",
  "password": "password123",
  "email": "user@example.com"
}
```

#### 登录 / Login
```
POST /api/auth/login
Content-Type: application/json

{
  "username": "user123",
  "password": "password123"
}

Response:
{
  "token": "jwt_token_here",
  "username": "user123"
}
```

### 钱包管理 / Wallet Management

#### 生成钱包 / Generate Wallet
```
POST /api/wallet/generate
Authorization: Bearer {token}
Content-Type: application/json

{
  "chain": "sepolia",
  "tokenAddress": null
}

Response:
{
  "address": "0x...",
  "qrCode": "data:image/png;base64,..."
}
```

#### 获取当前地址 / Get Current Address
```
GET /api/deposit/current-address?chain=sepolia&tokenAddress=
Authorization: Bearer {token}
```

### 充值管理 / Deposit Management

#### 获取待处理充值 / Get Pending Deposits
```
GET /api/deposit/pending
Authorization: Bearer {token}
```

#### 获取充值历史 / Get Deposit History
```
GET /api/deposit/history
Authorization: Bearer {token}
```

### 管理员 API / Admin API

#### 获取所有用户 / Get All Users
```
GET /api/admin/users
Authorization: Bearer {admin_token}
```

#### 获取所有钱包 / Get All Wallets
```
GET /api/admin/wallets
Authorization: Bearer {admin_token}
```

#### 获取钱包详情 / Get Wallet Details
```
GET /api/admin/wallets/{walletId}
Authorization: Bearer {admin_token}
```

#### 发送交易 / Send Transaction
```
POST /api/admin/wallets/{walletId}/send
Authorization: Bearer {admin_token}
Content-Type: application/json

{
  "toAddress": "0x...",
  "amount": "1000000000000000000",
  "tokenAddress": null
}
```

#### 获取所有交易 / Get All Transactions
```
GET /api/admin/transactions
Authorization: Bearer {admin_token}
```

---

## 前端界面 / Frontend Interface

### 用户充值页面 / User Deposit Page

**访问地址** / **Access URL**: `http://localhost:8080/deposit`

**功能** / **Features**:
- 用户登录/注册
- 钱包地址生成和显示
- QR 码生成
- 待处理充值列表
- 充值历史列表
- Etherscan 链接

### 管理员后台 / Admin Dashboard

**访问地址** / **Access URL**: `http://localhost:8080/admin`

**默认账号** / **Default Account**:
- 用户名 / Username: `admin`
- 密码 / Password: `admin`

**功能** / **Features**:
- 用户管理（查看所有用户）
- 钱包管理（查看所有钱包，包括私钥）
- 交易历史（查看所有充值交易）
- 交易发送（通过私钥发送交易）
- 搜索功能（按 ID 搜索）

---

## 安全特性 / Security Features

1. **密码加密** / **Password Encryption**: 使用 BCrypt 进行密码哈希
2. **私钥加密** / **Private Key Encryption**: 使用 AES-256 加密存储私钥
3. **JWT 认证** / **JWT Authentication**: 无状态的身份验证
4. **角色控制** / **Role-Based Access Control**: 基于角色的权限管理
5. **幂等性保证** / **Idempotency**: 防止重复处理交易

---

## 架构设计说明 / Architecture Design Notes

### 幂等性处理 / Idempotent Processing

系统使用阻塞队列和幂等性记录确保"精确一次"入账：

1. **监控服务** / **Monitor Service**: 扫描区块链，发现新交易
2. **阻塞队列** / **Blocking Queue**: 异步处理交易
3. **幂等性检查** / **Idempotency Check**: 检查 `DepositIdempotency` 表
4. **处理服务** / **Processor Service**: 处理交易并更新状态

### 钱包地址持久化 / Wallet Address Persistence

- 用户首次生成钱包后，地址会持久化保存
- 刷新页面或重新登录，地址保持不变
- 只有用户主动"刷新地址"才会生成新地址
- 旧地址标记为 `active=false`，但保留在数据库中

### 交易确认机制 / Transaction Confirmation Mechanism

- **PENDING**: 交易已检测，等待确认
- **CONFIRMING**: 1-11 个确认
- **CONFIRMED**: 12+ 个确认，可以入账
- **CREDITED**: 已成功入账
- **FAILED**: 处理失败

---

## 开发指南 / Development Guide

### 项目结构 / Project Structure

```
src/main/java/com/example/ethreader/
├── config/          # 配置类
├── controller/      # REST 控制器
├── dto/            # 数据传输对象
├── model/          # 数据模型
├── repository/     # 数据访问层
├── service/        # 业务逻辑层
└── util/           # 工具类

src/main/resources/
├── application.properties.example  # 配置示例
├── static/         # 静态资源（CSS, JS）
└── templates/      # 模板文件（HTML）
```

### 添加新功能 / Adding New Features

1. **添加新的 API 端点** / **Add New API Endpoint**:
   - 在 `controller` 包中创建或修改控制器
   - 在 `service` 包中实现业务逻辑
   - 在 `dto` 包中定义请求/响应对象

2. **添加新的数据模型** / **Add New Data Model**:
   - 在 `model` 包中创建实体类
   - 在 `repository` 包中创建仓库接口
   - 使用 Spring Data MongoDB 注解

3. **修改前端界面** / **Modify Frontend**:
   - 修改 `templates` 中的 HTML 文件
   - 更新 `static/css` 中的样式
   - 更新 `static/js` 中的 JavaScript

---

## 常见问题 / FAQ

### Q: 如何生成安全的密钥？
**A**: 使用 OpenSSL 或 PowerShell 脚本生成随机密钥。确保 JWT secret 至少 32 字节，encryption key 正好 32 字符。

### Q: 如何切换到主网？
**A**: 修改 `application.properties` 中的 `web3j.rpc.url` 为主网 RPC 节点 URL。

### Q: 私钥可以导入 MetaMask 吗？
**A**: 可以。管理员可以在后台查看私钥（64 位十六进制字符，无 0x 前缀），直接导入 MetaMask。

### Q: 如何修改确认数要求？
**A**: 修改 `application.properties` 中的 `blockchain.confirmations` 值。

---

## 许可证 / License

本项目采用 MIT 许可证。

This project is licensed under the MIT License.

---

## 贡献 / Contributing

欢迎提交 Issue 和 Pull Request！

Issues and Pull Requests are welcome!

---

## 面试题 / Interview Questions

### 1. 如何给用户生成公钥和私钥？/ How to Generate Public and Private Keys for Users?

**问题** / **Question**: 请详细说明系统中如何为用户生成以太坊钱包的公钥和私钥。

**答案** / **Answer**:

#### 技术实现 / Technical Implementation

系统使用 Web3j 库生成以太坊密钥对：

```java
// 1. 使用 Web3j 的 Keys 工具类生成椭圆曲线密钥对
ECKeyPair keyPair = Keys.createEcKeyPair();

// 2. 从密钥对创建凭证对象
Credentials credentials = Credentials.create(keyPair);

// 3. 获取以太坊地址（公钥的 Keccak-256 哈希的后20字节）
String address = credentials.getAddress();

// 4. 获取私钥（64位十六进制字符串）
String privateKeyHex = keyPair.getPrivateKey().toString(16);
```

#### 详细流程 / Detailed Process

1. **密钥生成算法** / **Key Generation Algorithm**:
   - 使用 **secp256k1** 椭圆曲线算法
   - 生成一个 256 位的随机私钥
   - 通过椭圆曲线点乘法计算公钥

2. **地址生成** / **Address Generation**:
   - 公钥是椭圆曲线上的一个点 (x, y)
   - 将公钥编码为 64 字节（去掉 0x04 前缀）
   - 对公钥进行 **Keccak-256** 哈希
   - 取哈希值的后 20 字节作为以太坊地址
   - 添加 0x 前缀

3. **私钥格式** / **Private Key Format**:
   - 私钥是 256 位的随机数
   - 转换为 64 位十六进制字符串
   - 确保格式正确（补零到 64 位，去除 0x 前缀）
   - 用于 MetaMask 导入兼容性

#### 代码位置 / Code Location

**文件**: `service/WalletService.java`

```java
public HotWallet generateHotWallet(String userId, String chain, String tokenAddress) {
    // 生成密钥对
    ECKeyPair keyPair = Keys.createEcKeyPair();
    Credentials credentials = Credentials.create(keyPair);
    
    // 获取地址（公钥）
    String address = credentials.getAddress();
    
    // 获取私钥并格式化
    String privateKeyHex = keyPair.getPrivateKey().toString(16);
    if (privateKeyHex.length() < 64) {
        privateKeyHex = String.format("%064s", privateKeyHex).replace(' ', '0');
    }
    if (privateKeyHex.startsWith("0x")) {
        privateKeyHex = privateKeyHex.substring(2);
    }
    
    // 加密私钥
    String encryptedPrivateKey = encryptionUtil.encrypt(privateKeyHex);
    
    // 保存到数据库
    // ...
}
```

---

### 2. 私钥如何安全存储？/ How to Securely Store Private Keys?

**问题** / **Question**: 系统如何确保私钥在数据库中的安全存储？

**答案** / **Answer**:

#### 加密方案 / Encryption Scheme

1. **加密算法**: AES-256 (Advanced Encryption Standard)
2. **加密模式**: AES/CBC/PKCS5Padding
3. **密钥管理**: 使用配置文件中的 `encryption.key`（32 字符）

#### 实现细节 / Implementation Details

```java
// 加密私钥
String encryptedPrivateKey = encryptionUtil.encrypt(privateKeyHex);

// 存储到数据库
wallet.setPrivateKey(encryptedPrivateKey);
```

**文件**: `util/EncryptionUtil.java`

- 使用 AES-256 对称加密
- 每次加密使用随机 IV（初始化向量）
- IV 与密文一起存储
- 解密时提取 IV 并解密

#### 安全考虑 / Security Considerations

- ✅ 私钥从不以明文形式存储
- ✅ 使用强加密算法（AES-256）
- ✅ 加密密钥独立于数据库存储
- ⚠️ 生产环境建议使用密钥管理服务（KMS）

---

### 3. 如何监控区块链上的充值交易？/ How to Monitor Deposit Transactions on Blockchain?

**问题** / **Question**: 系统如何实时监控区块链并检测充值交易？

**答案** / **Answer**:

#### 监控流程 / Monitoring Process

1. **区块扫描** / **Block Scanning**:
   ```java
   // 定期扫描新区块
   BigInteger currentBlock = blockchainService.getCurrentBlockNumber();
   for (BigInteger blockNum = lastScannedBlock; blockNum <= currentBlock; blockNum++) {
       EthBlock.Block block = blockchainService.getBlock(blockNum);
       scanBlockForTransactions(block);
   }
   ```

2. **交易检测** / **Transaction Detection**:
   - 遍历区块中的所有交易
   - 检查交易的 `to` 地址是否匹配我们的热钱包地址
   - 对于 ERC20 代币，解析 Transfer 事件

3. **事件解析** / **Event Parsing**:
   ```java
   // ERC20 Transfer 事件签名
   String TRANSFER_EVENT_SIGNATURE = "0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef";
   
   // 解析日志中的 Transfer 事件
   for (EthLog.LogResult logResult : receipt.getLogs()) {
       if (isTransferEvent(logResult)) {
           // 提取 to 地址和 amount
       }
   }
   ```

#### 架构设计 / Architecture Design

```
Blockchain → BlockchainService → DepositMonitorService 
    → BlockingQueue → DepositProcessorService → MongoDB
```

**文件**: 
- `service/BlockchainService.java` - 区块链交互
- `service/DepositMonitorService.java` - 监控服务
- `service/DepositProcessorService.java` - 处理服务

---

### 4. 如何保证幂等性？/ How to Ensure Idempotency?

**问题** / **Question**: 系统如何防止同一笔交易被重复处理？

**答案** / **Answer**:

#### 幂等性机制 / Idempotency Mechanism

1. **唯一标识** / **Unique Identifier**:
   - 使用交易哈希（transaction hash）作为唯一标识
   - 交易哈希在区块链上是全局唯一的

2. **幂等性记录** / **Idempotency Record**:
   ```java
   // 检查是否已处理
   Optional<DepositIdempotency> existing = 
       depositIdempotencyRepository.findByTransactionHash(transactionHash);
   
   if (existing.isPresent() && existing.get().isProcessed()) {
       // 已处理，跳过
       return;
   }
   ```

3. **原子性操作** / **Atomic Operation**:
   - 先检查幂等性记录
   - 如果不存在，创建记录并标记为处理中
   - 处理完成后更新状态
   - 使用数据库事务确保原子性

#### 数据模型 / Data Model

```java
public class DepositIdempotency {
    private String id;
    private String transactionHash;  // 唯一标识
    private boolean processed;       // 是否已处理
    private LocalDateTime processedAt;
}
```

**文件**: `service/DepositProcessorService.java`

---

### 5. 交易确认机制是如何工作的？/ How Does Transaction Confirmation Work?

**问题** / **Question**: 系统如何判断交易已确认并可以入账？

**答案** / **Answer**:

#### 确认数计算 / Confirmation Calculation

```java
// 获取当前区块号
BigInteger currentBlock = blockchainService.getCurrentBlockNumber();

// 计算确认数
int confirmations = currentBlock.subtract(transactionBlockNumber).intValue();

// 更新确认数
depositTransaction.setConfirmations(confirmations);
```

#### 状态转换 / Status Transition

1. **PENDING** (0 确认): 交易已检测，等待确认
2. **CONFIRMING** (1-11 确认): 交易确认中
3. **CONFIRMED** (12+ 确认): 交易已确认，可以入账
4. **CREDITED** (已入账): 已成功入账
5. **FAILED** (失败): 处理失败

#### 配置参数 / Configuration

```properties
# 需要多少个确认才能入账
blockchain.confirmations=12

# 确认数更新间隔（毫秒）
blockchain.scan.interval=5000
```

**文件**: `service/ScheduledTasks.java`

---

### 6. 为什么使用阻塞队列？/ Why Use Blocking Queue?

**问题** / **Question**: 系统为什么使用阻塞队列而不是直接处理交易？

**答案** / **Answer**:

#### 优势 / Advantages

1. **解耦** / **Decoupling**:
   - 监控服务和处理服务解耦
   - 监控服务专注于发现交易
   - 处理服务专注于业务逻辑

2. **异步处理** / **Asynchronous Processing**:
   - 监控服务不会被处理逻辑阻塞
   - 可以持续扫描新区块

3. **背压控制** / **Backpressure Control**:
   - 当处理速度慢时，队列会阻塞
   - 防止内存溢出

4. **可靠性** / **Reliability**:
   - 即使处理服务暂时不可用，交易也会保存在队列中
   - 服务恢复后可以继续处理

#### 实现 / Implementation

```java
// 使用 LinkedBlockingQueue
private BlockingQueue<DepositTransaction> depositQueue = 
    new LinkedBlockingQueue<>();

// 监控服务：放入队列
depositQueue.put(depositTransaction);

// 处理服务：从队列取出
DepositTransaction deposit = depositQueue.take();
```

**文件**: `service/DepositMonitorService.java`, `service/DepositProcessorService.java`

---

### 7. 如何处理 ERC20 代币转账？/ How to Handle ERC20 Token Transfers?

**问题** / **Question**: 系统如何检测和处理 ERC20 代币转账？

**答案** / **Answer**:

#### 事件检测 / Event Detection

1. **Transfer 事件签名**:
   ```java
   // Transfer(address indexed from, address indexed to, uint256 value)
   String TRANSFER_EVENT_SIGNATURE = 
       "0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef";
   ```

2. **日志解析**:
   ```java
   // 遍历交易收据中的日志
   for (EthLog.LogResult logResult : receipt.getLogs()) {
       EthLog.Log log = (EthLog.Log) logResult.get();
       
       // 检查事件签名
       if (TRANSFER_EVENT_SIGNATURE.equals(log.getTopics().get(0))) {
           // 解析 to 地址和 amount
           String toAddress = extractAddress(log.getTopics().get(2));
           BigInteger amount = extractAmount(log.getData());
       }
   }
   ```

3. **地址匹配**:
   - 检查 `to` 地址是否匹配我们的热钱包地址
   - 检查代币合约地址是否匹配用户选择的代币

**文件**: `service/BlockchainService.java`

---

### 8. 热钱包和冷钱包的区别？/ What's the Difference Between Hot and Cold Wallets?

**问题** / **Question**: 本系统使用的是热钱包还是冷钱包？有什么区别？

**答案** / **Answer**:

#### 热钱包 / Hot Wallet

**本系统使用热钱包**，特点：

1. **在线存储** / **Online Storage**:
   - 私钥存储在服务器数据库中
   - 可以快速访问和操作

2. **自动化** / **Automation**:
   - 可以自动接收充值
   - 可以自动发送交易（管理员操作）

3. **风险** / **Risks**:
   - 服务器被攻击可能导致私钥泄露
   - 需要严格的安全措施

#### 冷钱包 / Cold Wallet

特点：

1. **离线存储** / **Offline Storage**:
   - 私钥存储在离线设备（硬件钱包、纸钱包）
   - 不连接到互联网

2. **安全性** / **Security**:
   - 更高的安全性
   - 但操作不便

3. **适用场景** / **Use Cases**:
   - 大额资金存储
   - 长期持有

#### 本系统的设计 / System Design

- ✅ 使用热钱包便于自动化充值监控
- ✅ 私钥加密存储
- ✅ 支持管理员通过私钥发送交易
- ⚠️ 生产环境建议定期将资金转移到冷钱包

---

### 9. 如何防止重放攻击？/ How to Prevent Replay Attacks?

**问题** / **Question**: 系统如何防止交易被重放？

**答案** / **Answer**:

#### 防护机制 / Protection Mechanisms

1. **交易哈希唯一性**:
   - 每笔交易都有唯一的哈希
   - 通过幂等性检查防止重复处理

2. **Nonce 机制**:
   - 以太坊交易包含 nonce
   - 每个地址的 nonce 必须递增
   - 已使用的 nonce 不能重复

3. **链 ID**:
   - 交易包含链 ID
   - 防止跨链重放

#### 系统实现 / System Implementation

```java
// 通过交易哈希检查是否已处理
Optional<DepositIdempotency> existing = 
    depositIdempotencyRepository.findByTransactionHash(transactionHash);

if (existing.isPresent() && existing.get().isProcessed()) {
    // 已处理，拒绝重复处理
    return;
}
```

---

### 10. 如何处理网络分叉？/ How to Handle Network Forks?

**问题** / **Question**: 如果区块链发生分叉，系统如何处理？

**答案** / **Answer**:

#### 分叉处理 / Fork Handling

1. **确认数要求**:
   - 要求 12 个确认数
   - 分叉通常只有几个区块
   - 12 个确认后，分叉链被丢弃的概率很高

2. **最长链原则**:
   - 以太坊遵循最长链原则
   - 系统总是跟随最长链

3. **重新扫描**:
   - 如果检测到分叉，可以重新扫描区块
   - 更新交易状态

#### 当前实现 / Current Implementation

- ✅ 使用 12 个确认数降低分叉风险
- ⚠️ 可以添加分叉检测机制
- ⚠️ 可以添加区块重组处理

---

### 11. 系统如何处理高并发？/ How Does the System Handle High Concurrency?

**问题** / **Question**: 当大量用户同时充值时，系统如何保证性能？

**答案** / **Answer**:

#### 并发处理 / Concurrency Handling

1. **异步处理**:
   - 使用阻塞队列异步处理交易
   - 监控服务和处理服务分离

2. **数据库优化**:
   - 使用 MongoDB 的索引
   - 交易哈希唯一索引
   - 用户ID索引

3. **批量处理**:
   - 可以批量处理队列中的交易
   - 减少数据库操作

#### 性能优化 / Performance Optimization

```java
// 批量处理队列中的交易
List<DepositTransaction> batch = new ArrayList<>();
int batchSize = 10;

while (!depositQueue.isEmpty() && batch.size() < batchSize) {
    DepositTransaction deposit = depositQueue.poll();
    if (deposit != null) {
        batch.add(deposit);
    }
}

// 批量保存到数据库
depositTransactionRepository.saveAll(batch);
```

---

### 12. 如何保证数据一致性？/ How to Ensure Data Consistency?

**问题** / **Question**: 系统如何保证数据库和区块链数据的一致性？

**答案** / **Answer**:

#### 一致性保证 / Consistency Guarantees

1. **事务处理**:
   - 使用 MongoDB 事务
   - 确保幂等性记录和交易记录同时更新

2. **状态机**:
   - 交易状态有明确的转换规则
   - 防止状态不一致

3. **定期同步**:
   - 定期检查区块链上的交易状态
   - 更新本地数据库

#### 实现 / Implementation

```java
// 使用事务确保原子性
@Transactional
public void processDeposit(DepositTransaction deposit) {
    // 1. 创建幂等性记录
    DepositIdempotency idempotency = new DepositIdempotency();
    idempotency.setTransactionHash(deposit.getTransactionHash());
    idempotency.setProcessed(true);
    depositIdempotencyRepository.save(idempotency);
    
    // 2. 更新交易状态
    deposit.setStatus(DepositStatus.CREDITED);
    depositTransactionRepository.save(deposit);
}
```

---

## 联系方式 / Contact

如有问题，请提交 Issue。

For questions, please submit an Issue.

