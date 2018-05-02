#ifndef BITSTREAM_H
#define BITSTREAM_H

#include<string>
#include<iostream>
#include<iomanip>
#include<bitset>
#include<vector>
#include<boost/dynamic_bitset.hpp>
#include<boost/unordered_map.hpp>
#include<boost/lexical_cast.hpp>
#include<boost/algorithm/string/classification.hpp>
#include<boost/algorithm/string/split.hpp>
#include"architecture.h"
#include"device.h"
#include"tile.h"
#include"v4_devices.h"
#include"v5_devices.h"
#include "HTypes.h"

namespace nmt
{
	using std::size_t;
	using std::fstream;
	using std::cerr;
	using std::iostream;
	using namespace std;
/** @mainpage
 * The bitLib bitstream library provides access to XDL configuration settings at the bitstream level.
 * Currently the library supports Xilinx Virtex4 (routing and CLB logic) and Virtex5 (routing) devices.
 * For information on how to use the library, see the bit_test.cc and v5_bit_test.cc examples, as well
 * as the bitstream class documentation.
 */
/** 
 * The bitstream class acts as the interface/controller class for bitLib.  Most useful bitstream exploration
 * can be done via the public functions of this class.
 * A typical use case for this class will look like this:
 * @li Pass a device name to the bitstream::bitstream(string device_name) constructor
 * @li Call the bitstream::mapBitstream function
 * @li Call the bistream::loadFile function with the bitstream filename as the parameter.
 * @li Use the bitstream::getArc,bitstream::setArc,bitstream::getCfg,bitstream::setCfg functions
 * to view or modify routing and logic configuration bits.
 * 
 * Examples of how to use the bitstream class can be found in bit_test.cc and v5_bit_test.cc
 */
	class bitstream
	{
	protected:
		string bitstreamFile;
		bool frameECC;
		device *my_dev;
		char **frame_array;
		uint8 *mFrameData;
		int num_frames;
		/** Map between frame indices and frameAddresses for fast lookup */
		frame_addr *findexToFaddr;
		boost::unordered_map < tile_coord, tile_data*, boost::hash < tile_coord > >tile_map;
		boost::unordered_map < std::string, tile_data*, boost::hash < std::string > >tileMap;
		string designName;
		string deviceName;
		string designDate;
		string designTime;
		uint32 bitstreamLength;
		uint32 bitstreamWordCount;
		size_t cfgMemoryStart;
		bool isPartial;
		vector<uint32> changedFrames;
		boost::dynamic_bitset<> frameBitmap;
		/**
		 * Build a mapping from frame index to frame address.
		 * @param majorAddress Frame Address for the major column
		 * @param startIndex Starting Index for the tile frames 
		 * @param tileFrames number of frame for the tileType
		 */
		void buildItoaMap(frame_addr majorAddress, uint32 startIndex, uint32 tileFrames);
		/**
		 * Fill in the appropriate addresses for GCLK columns.
		 */
		void buildGCLKItoaMap();
		static bool expect(fstream& inStream, uint8 inExpected);
		static bool expect(fstream& inStream, uint16 inExpected);
		static bool expect(fstream& inStream, uint32 inExpected);
		static bool expect(fstream& inStream, const string& inExpected);
		static void readXilinxString(fstream& inStream, string& outString);
		static bool readHeader(fstream& inStream, string& outDesignName, string& outDeviceName, string& outDesignDate, 
			string& outDesignTime, uint32& outBitstreamLength);
		static bool write(fstream& outStream, uint8 outVal);
		static bool write(fstream& outStream, uint16 outVal);
		static bool write(fstream& outStream, uint32 outVal);
		static bool write(fstream& outStream, const string& outVal);
		static void writeXilinxString(fstream& outStream, string inString);
		bool writeHeader(fstream& outStream);
		virtual bool readPackets(fstream& inStream) = 0;
		virtual bool writePackets(fstream& outStream) = 0;
		virtual bool writePacketsPartial(fstream& outStream) = 0;
		/**
		 * Write a packet header to the bitstream.
		 * @param outStream Bitstream to be written.
		 * @param packetType uint indicating type 1 or type 2 packet.
		 * @param opcode Operation to be performed on register.
		 * @param address Register address to be written.
		 * @param reserved Param for reserved opcodes.
		 * @param count Number of words to be written in packet.
		 */ 
		virtual bool writePacketHeader(fstream& outStream, uint32 packetType, uint32 opcode, uint32 address, uint32 reserved, uint32 count) = 0;
		virtual frame_addr farToStruct(uint32 far) = 0;
		virtual uint32 structToFar(frame_addr far) = 0;
		virtual bool writeFrameData(fstream& inStream, int inCount) = 0;
		virtual void prepFrameForWrite(uint32 fIndex) {};
		uint32 calculateFrameECC(uint32 fIndex);
		uint32 swapEndian(const uint32 source) const; 
		/**
		 * Null constructor used by the standalone bitstream reader
		 */
		bitstream (void) : my_dev(NULL), frame_array(NULL), mFrameData(NULL), findexToFaddr(NULL), isPartial(NULL) {}
	public:
		/**
		 * Create and return a new bitstream compatible with the supplied bitstream file.
		 * @param inFilename Name of the reference bitstream.
		 */
		static bitstream* newBitstream(const string& inFilename);
		/**
		 * Allocate device object based on supplied name.
		 * @param device_name Specific part to be used.
		 */
		bitstream (string device_name, bool frame_ecc = false);
		/**
		 * Allocate memory for frame_array and sequentially load 
		 * bitstream.
		 * @param bitstream_name File name of bitstream to load
		 */
		bool loadFile (string bitstream_name);
		/**
		 * Write configuration memory back to bitstream.
		 */
		bool writeCfgMemory(string bitstreamName);
		/**
		 * Write out partial bitstream.
		 * @param outStream bitstream file to be written
		 */
		virtual bool writePartialFrames(fstream& outStream) = 0;
		/**
		 * Write out frames to to outStream file.
		 * @param outStream Bitstream file to be written
		 * @param startFrame first frame to be written
		 * @param numFrames number of frames to be written
		 */
		bool writeFrames(fstream& outStream, int startFrame, int numFrames);
		/**
		 * Write a bitstream to the specified file.
		 * @param outBitstream Bitstream file to be written.
		 */
		bool writeBitstream(const string& outBitstream);
		/**
		 * Build tilemap using CDB.
		 */
		int buildTileMap ();
		/**
		 * Map tile coordinates to frame_array indices
		 */
		int mapBitstream ();
		/**
		 * Map bram to tile name.
		 * @param x X Coordinate of BRAM
		 * @param y Y Coordinate of BRAM.
		 */
		int mapBRAM(int x, int y);
		/**
		 * Extract XDL tile type from tileName and return string.
		 * @param tileName string containing XDL name of tile.
		 */
		std::string extractXDLType(string tileName);
		/**
		 * Extract XDL coordinates from tileName and return tile_coord object.
		 * @param tileName string containing XDL name of tile.
		 */
		tile_coord extractXDLCoord(string tileName);

		/**
		 * Build an XDL name using the tile type and tile coordinate.
		 * @param tileType String containing tile type info.
		 * @param coord XDL coordinates of tile.
		 */
		string buildXDLName(string tileType, tile_coord coord);
		/**
		 * Take a tileName string and return the tile_data struct that corresponds
		 * to it.
		 * @param tileName string containing the XDL name of the tile.
		 */
		tile_data * getTileData(string tileName);
		/**
		 * Return struct containing Frame Address, Frame Number, Byte Offset,
		 * and number of frames the tile spans along with a pointer to the 
		 * first frame in the frame_array with data for this tile
		 * @param x X Coordinate of Tile
		 * @param y Y Coordinate of Tile
		 */
		tile_data * getTileData(int x, int y);
		/**
		 * Dump all frames in the bitstream in a human readable manner.
		 */
		void dumpFrames();
		/**
		 * Dump the requested frames to stdout.
		 * @param startFrame The first frame to print out.
		 * @param endFrame The last frame to print + 1.
		 */
		void dumpFrames(uint32 startFrame, uint32 endFrame);
		/**
		 * Compare two bitstreams and find the bits that differ.
		 * @param bitstreamA The first bitstream to compare.
		 * @param bitstreamB The second bitstream to compare.
		 */
		static void compareBitstreams(const bitstream& bitstreamA, const bitstream& bitstreamB);
		/**
		 * Build vector representing all frames in static bitstream.
		 * @param regionTiles vector of all tiles in the region, use CDB to generate this list.
		 */
		void buildPartial(vector<string> regionTiles);
		/**
		 * Compare two unsigned bytes of data and find the bits that differ.
		 * @param a The first byte to compare.
		 * @param b The second byte to compare.
		 */
		static void compareBytes(const uint8 a, const uint8 b);
		/**
		 * Free all dynamically allocated memory
		 */
		~bitstream();
	};
}
#endif
