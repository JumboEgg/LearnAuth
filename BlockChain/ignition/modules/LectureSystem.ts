import { ethers } from "hardhat";

async function main() {
  const [deployer] = await ethers.getSigners();
  console.log("Deploying contracts with the account:", deployer.address);

  // Deploy the Forwarder first
  const MinimalForwarder = await ethers.getContractFactory("MinimalForwarder");
  const forwarder = await MinimalForwarder.deploy();
  await forwarder.waitForDeployment();
  console.log("MinimalForwarder deployed to:", await forwarder.getAddress());

  // Deploy the CAT Token
  const CATToken = await ethers.getContractFactory("CATToken");
  const catToken = await CATToken.deploy(await forwarder.getAddress());
  await catToken.waitForDeployment();
  console.log("CATToken deployed to:", await catToken.getAddress());

  // Deploy the LectureSystem
  const LectureSystem = await ethers.getContractFactory("LectureSystem");
  const lectureSystem = await LectureSystem.deploy(await catToken.getAddress(), await forwarder.getAddress());
  await lectureSystem.waitForDeployment();
  console.log("LectureSystem deployed to:", await lectureSystem.getAddress());

  // Mint some tokens to deployer for testing
  const mintAmount = ethers.parseEther("1000000000"); // Mint 1,000,000,000 tokens
  await catToken.mint(deployer.address, mintAmount);
  console.log(`Minted ${ethers.formatEther(mintAmount)} CAT tokens to deployer`);
}

main()
  .then(() => process.exit(0))
  .catch((error) => {
    console.error(error);
    process.exit(1);
  });