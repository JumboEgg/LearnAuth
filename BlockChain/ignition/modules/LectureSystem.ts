import hre from "hardhat";
import dotenv from "dotenv";
import fs from "fs";
import path from "path";

async function main() {
  // Load environment variables
  dotenv.config();

  // Validate environment variables
  const privateKey = process.env.PRIVATE_KEY;
  if (!privateKey) {
    throw new Error("Please set PRIVATE_KEY in your .env file");
  }

  // Get the deployer wallet
  const deployer = new hre.ethers.Wallet(privateKey, hre.ethers.provider);
  console.log("Deploying contracts with account:", deployer.address);

  // Check deployer's balance
  const balance = await deployer.getBalance();
  console.log("Account balance:", hre.ethers.utils.formatEther(balance), "MATIC");

  // Gas configuration for Polygon
  const gasConfig = {
    gasPrice: hre.ethers.utils.parseUnits('100', 'gwei'), // 100 Gwei 가스 가격
    gasLimit: 5000000 // 충분한 가스 리밋 설정
  };

  // Deploy LectureForwarder
  const LectureForwarder = await hre.ethers.getContractFactory("LectureForwarder");
  const forwarder = await LectureForwarder.connect(deployer).deploy({...gasConfig});
  await forwarder.deployed();
  console.log("LectureForwarder deployed to:", forwarder.address);

  // Deploy CATToken
  const CATToken = await hre.ethers.getContractFactory("CATToken");
  const catToken = await CATToken.connect(deployer).deploy(forwarder.address, {...gasConfig});
  await catToken.deployed();
  console.log("CATToken deployed to:", catToken.address);

  // Deploy LectureSystem
  const LectureSystem = await hre.ethers.getContractFactory("LectureSystem");
  const lectureSystem = await LectureSystem.connect(deployer).deploy(
    catToken.address, 
    forwarder.address,
    {...gasConfig}
  );
  await lectureSystem.deployed();
  console.log("LectureSystem deployed to:", lectureSystem.address);

  // Grant minter role to deployer
  const MINTER_ROLE = await catToken.MINTER_ROLE();
  await catToken.grantRole(MINTER_ROLE, deployer.address, {...gasConfig});
  console.log("Minter role granted to deployer");

  // Save contract addresses to a JSON file for frontend/reference
  const contractAddresses = {
    LectureForwarder: forwarder.address,
    CATToken: catToken.address,
    LectureSystem: lectureSystem.address
  };

  // Ensure the directory exists
  const outputDir = path.join(__dirname, '../deployments');
  if (!fs.existsSync(outputDir)) {
    fs.mkdirSync(outputDir);
  }

  // Write contract addresses to a JSON file
  fs.writeFileSync(
    path.join(outputDir, `${hre.network.name}-deployed-contracts.json`), 
    JSON.stringify(contractAddresses, null, 2)
  );

  console.log("Contract addresses saved to deployments directory");
}

// Hardhat recommended pattern for handling async deployment
main()
  .then(() => process.exit(0))
  .catch((error) => {
    console.error(error);
    process.exit(1);
  });