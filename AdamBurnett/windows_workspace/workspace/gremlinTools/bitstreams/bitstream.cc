// Gizmo - Copyright 2009 University of Southern California.  All Rights Reserved.
// $HeadURL: https://svn.east.isi.edu/gremlin/trunk/gizmo/tools/bitstreamTools/partialBitstreamGenerator/bitstream.cc $
// $Id: bitstream.cc 717 2010-06-09 06:17:20Z asohangh $

#include "bitstream.h"
#include "v4_bitstream.h"
#include "v5_bitstream.h"
#include <fstream>
#include <netinet/in.h>
#include <boost/regex.hpp>

namespace nmt
{
	bitstream::bitstream (string device_name, bool frame_ecc) : my_dev(NULL), frame_array(NULL), findexToFaddr(NULL), isPartial(false)
	{
		// preinitialize variables
		designName.clear();
		deviceName.clear();
		designDate.clear();
		designTime.clear();
		bitstreamLength = 0;
		bitstreamWordCount = 0;

		// If the constructor was passed a valid device, create the device object
		if (device_name == "xc4vlx15")
			my_dev = new xc4vlx15 ();
		else if (device_name == "xc4vfx60")
			my_dev = new xc4vfx60 ();
		else if (device_name == "xc4vlx60")
			my_dev = new xc4vlx60 ();
		else if (device_name == "xc5vlx50")
			my_dev = new xc5vlx50 ();
		else if (device_name == "xc5vlx50t")
			my_dev = new xc5vlx50t ();
		else if (device_name == "xc5vsx95t")
			my_dev = new xc5vsx95t ();
		else if (device_name == "xc5vlx110t")
			my_dev = new xc5vlx110t ();

		else
			fprintf(stderr,"Error, device %s is not supported\n",device_name.c_str());
		// If the device was created successfully, allocate memory for frame data
		// and the findexToFaddr array
		if(my_dev)
		{
			uint32 cfgSize = my_dev->get_cfg_size();
			uint32 wordCount = cfgSize * my_dev->get_frame_words();
			uint32 byteCount = wordCount << 2;
			uint32 bytesPerFrame = my_dev->get_frame_words() << 2;
			
			frame_array = new char*[cfgSize];
			mFrameData = new uint8[byteCount];
			uint8* ptr = mFrameData;
			if(!mFrameData)
				cerr << "mFrameData not allocated in constructor" << endl;
			for(uint32 frame = 0; frame < cfgSize; frame++, ptr += bytesPerFrame)
			 frame_array[frame] = (char*)ptr;	
			findexToFaddr = new frame_addr[my_dev->get_cfg_size()];
			frameECC = frame_ecc;
		}
	}
/*
	int
	bitstream::buildTileMap()
	{
		adb::CBitstreamV5 & mBS = my_dev->getCBitstream();
		std::multimap<tile_types,string> tileToTT;
		vector<string> tileNames = mBS.getTileNames();
		for(uint32 i = 0; i < tileNames.size(); i++)
		{
			tile_coord curCoord = extractXDLCoord(tileNames[i]);
			tile_types eTT = my_dev->get_tile_type(curCoord.x);
			string tileType = extractXDLType(tileNames[i]);
			std::multimap<tile_types,string>::iterator it = tileToTT.find(eTT);
			bool ttExists = false;
			while(it != tileToTT.end())
			{
				if(it->second == tileType)
					ttExists = true;
				it++;
			}
			if(!ttExists)
				tileToTT.insert(std::pair<tile_types,string>(eTT,tileType));
		}
		cout << "printing tileMap ";
		int tt_prev = -1;
		for(std::multimap<tile_types,string>::iterator it = tileToTT.begin(); it != tileToTT.end(); it++)
		{
			if(it->first != tt_prev)
			{
				tt_prev = it->first;
				cout << endl;
				cout << it->first << "\t";
			}
			else
				cout << it->second << "\t";
		}
		return 0;
	}
*/
	int
	bitstream::mapBitstream()
	{
		int x = 0;
		int y = 0;
		int frame_num;
		frame_addr far;
		tile_data * tile;
		printf("Chip has height of %d and width of %d\n",my_dev->chip_height(),
				my_dev->chip_width());
		// Print out the CFG sizes of the different block types
		for(int i = 0; i < my_dev->device::get_addressable_blk_types(); i++)
			printf("CFG Size of blktype %d is %d\n",i, my_dev->get_cfg_size(i));

		printf("Total CFG Size is %d frames\n",my_dev->get_cfg_size());

		for(y = 0; y < my_dev->chip_height(); y++)
		{
			for(x=0;x<my_dev->chip_width();x++)
			{
				far = my_dev->tilecoord_to_major(x,y);
				frame_num = my_dev->frame_offset(far);
				// If frame is of type BRAM_INT, add cfg_size of CLB block
				for(int i=0;i<my_dev->get_blk_type(x);i++)
					frame_num += my_dev->get_cfg_size(i);
				buildItoaMap(far, frame_num, my_dev->get_tile_frames(x));				
				if(false)
				{
					printf("Tile (%d,%d) has frame address %d_%d_%d_%d_%d\n",
							x, y,far.type, far.tb, far.row, far.col, 0);
					printf("Frame offset is %d\n",frame_num);
					printf("Byte offset within frame is %d\n",my_dev->tile_offset(x,y));
					printf("Tile spans %d frames\n",my_dev->get_tile_frames(x));
				}
				tile = new tile_data("",x,y,far,my_dev->get_tile_frames(x),frame_num,my_dev->tile_offset(x,y),frame_array[frame_num]);
				tile_map[tile->coord] = tile;
				int tile_type = my_dev->get_tile_type(x);
				if(tile_type == IOB)
				{
					tileMap[buildXDLName("INT",tile->coord)] = tile;
					cout << buildXDLName("INT",tile->coord) << endl;
				}
				else if(tile_type == CLB)
				{
					tileMap[buildXDLName("INT",tile->coord)] = tile;
					tileMap[buildXDLName("CLB",tile->coord)] = tile;
					tileMap[buildXDLName("CLBLL",tile->coord)] = tile;
					tileMap[buildXDLName("CLBLM",tile->coord)] = tile;
				}
				else if(tile_type == DSP48)
				{
					tileMap[buildXDLName("INT",tile->coord)] = tile;
					tileMap[buildXDLName("DSP",tile->coord)] = tile;
				}
				else if(tile_type == BRAM_INT)
				{
					tileMap[buildXDLName("INT",tile->coord)] = tile;
					mapBRAM(x,y);
				}
				else if(tile_type == TRANSCV)
				{
					tileMap[buildXDLName("INT",tile->coord)] = tile;
				}
			}
		}
		if(false)
		{
			for(int i=0;i<my_dev->get_cfg_size();i++)
				printf("Frame %d, has FAR %s\n",i,findexToFaddr[i].str().c_str());
		}
		buildGCLKItoaMap();
		return 0;	
	}
	int
	bitstream::mapBRAM(int x, int y)
	{
		if(my_dev->get_tile_type(x) != BRAM_INT)
			return -1;
		frame_addr far = my_dev->bramcoord_to_major(x,y);
		int frame_num = my_dev->frame_offset(far);
		// Add offset to start of BRAM region in bitstream
		for(int i=0;i<my_dev->get_blk_type(BRAM);i++)
			frame_num += my_dev->get_cfg_size(i);
		buildItoaMap(far, frame_num, my_dev->get_tile_frames(BRAM));				
		if(false)
		{
			printf("Tile (%d,%d) has frame address %d_%d_%d_%d_%d\n",
					x, y,far.type, far.tb, far.row, far.col, 0);
			printf("Frame offset is %d\n",frame_num);
			printf("Byte offset within frame is %d\n",my_dev->tile_offset(x,y));
			printf("Tile spans %d frames\n",my_dev->get_tile_frames(BRAM));
			farToStruct(structToFar(far));
		}
		tile_data* tile = new tile_data("",x,y,far,my_dev->get_tile_frames(BRAM),frame_num,my_dev->tile_offset(x,y),frame_array[frame_num]);
		tileMap[buildXDLName("BRAM",tile->coord)] = tile;
		return 0;
	}
	
	void
	bitstream::buildItoaMap(frame_addr majorAddress, uint32 startIndex, uint32 tileFrames)
	{
		if(findexToFaddr[startIndex].type != -1)
			return;
		for(uint32 i=0;i<tileFrames;i++)
		{
			majorAddress.mna = i;
			findexToFaddr[startIndex+i] = majorAddress;
		}
	}

	void
	bitstream::buildGCLKItoaMap()
	{
		// For every row find GCLK column and fill in frame_index to frame_offset translation
		int x = my_dev->get_gclk_index();
		for(int row=0;row<my_dev->get_num_rows();row++)
		{
			cout << "buildGCLKItoaMap: loop " << row << endl;
			int y = row * my_dev->get_row_height();
			frame_addr gclkAddr = my_dev->tilecoord_to_major(x,y);
			int offset = my_dev->frame_offset(gclkAddr) + my_dev->get_tile_frames(my_dev->get_tile_type(x));
			cout << "buildGCLKItoaMap: offset " << offset << endl;
			gclkAddr.col++;
			for(int i=offset;i<offset+my_dev->get_tile_frames(GCLK);i++)
			{
				findexToFaddr[i] = gclkAddr;
				gclkAddr.mna++;
				cout << "Setting index " << i << " to " << gclkAddr.str() << endl;
			}
		}
	}

	string
	bitstream::buildXDLName(string tileType, tile_coord coord)
	{
		ostringstream retVal;
		retVal << tileType << "_X" << coord.x << "Y" << coord.y;
		return retVal.str();
	}

	string
	bitstream::extractXDLType(string tileName)
	{
		static const boost::regex extractTileInfo("(.*)_X(\\d*)Y(\\d*)");
		boost::smatch matches;
		if(regex_match(tileName, matches, extractTileInfo))
			return matches[1];
		else
			return "";
	}
	tile_coord
	bitstream::extractXDLCoord(string tileName)
	{
		static const boost::regex extractTileInfo("(.*)_X(\\d*)Y(\\d*)");
		boost::smatch matches;
		int x,y;
		if(regex_match(tileName, matches, extractTileInfo))
		{
		//	cout << "extract: " << matches[1] << endl;
			x = boost::lexical_cast<int>(matches[2]);
			y = boost::lexical_cast<int>(matches[3]);
		}
		tile_coord retVal(x,y);
		return retVal;
	}

	tile_data *
	bitstream::getTileData(string tileName)
	{
		//cout << "Tile: " << splitTileName[0] << "@" << x << "," << y << endl;
		tile_coord coord = extractXDLCoord(tileName);
		return getTileData(coord.x,coord.y);	
	}

	tile_data *
	bitstream::getTileData(int x, int y)
	{
		tile_coord coord(x,y);
		if(tile_map.count(coord))
			return tile_map[coord];
		else
			return NULL;
	}

	bitstream::~bitstream()
	{
		boost::unordered_map < tile_coord, tile_data* >::iterator tile_iterator;
		for(tile_iterator = tile_map.begin(); tile_iterator != tile_map.end();
				tile_iterator++)
		{
						//tile_iterator->second->print();
						delete tile_iterator->second;
						tile_iterator->second = NULL;
		}
		if(findexToFaddr != NULL) { delete [](findexToFaddr); findexToFaddr = NULL; }
		if(mFrameData != NULL) { delete[](mFrameData); mFrameData = NULL; }
		if(frame_array != NULL) { delete[](frame_array); frame_array = NULL; }
		if(my_dev != NULL) { delete(my_dev); my_dev = NULL; }
	}

	bool bitstream::expect(fstream& inStream, uint8 inExpected) {
		// read the actual data from the stream
		uint8 actual = 0;
		inStream.read((char*) &actual, sizeof(actual));
		// return equality
		return inExpected == actual;
	}

	bool bitstream::expect(fstream& inStream, uint16 inExpected) {
		// read the actual data from the stream
		uint16 actual = 0;
		inStream.read((char*) &actual, sizeof(actual));
		// return equality
		return inExpected == ntohs(actual);
	}

	bool bitstream::expect(fstream& inStream, uint32 inExpected) {
		// read the actual data from the stream
		uint32 actual = 0;
		inStream.read((char*) &actual, sizeof(actual));
		// return equality
		return inExpected == ntohl(actual);
	}

	bool bitstream::expect(fstream& inStream, const string& inExpected) {
		// look for the length;
		if(!expect(inStream, (uint16) inExpected.size())) return false;
		// create a buffer to use
		int adjustedLength = inExpected.size() + 1;
		char* buffer = new char[adjustedLength];
		// read the actual data from the stream
		inStream.read(buffer, adjustedLength);
		return buffer == inExpected;
	}

	void bitstream::readXilinxString(fstream& inStream, string& outString) {
		// read the string length
		uint16 length = 0;
		inStream.read((char*) &length, sizeof(length));
		length = ntohs(length);
		if(length > 0) {
			// create a buffer
			char* buffer = new char[length];
			// read the null-terminated string
			inStream.read(buffer, length);
			// copy the data into the string
			outString.assign(buffer, length - 1);
			delete[] buffer;
		} else {
			outString.clear();
		}
	}

	bool bitstream::readHeader(fstream& inStream, string& outDesignName, string& outDeviceName, string& outDesignDate, 
		string& outDesignTime, uint32& outBitstreamLength) {
		// assume success until we find otherwise
		bool success = true;
		// read the magic length
		success &= expect(inStream, (uint16) 0x0009);
		// read the magic bytes
		success &= expect(inStream, (uint32) 0x0ff00ff0);
		success &= expect(inStream, (uint32) 0x0ff00ff0);
		success &= expect(inStream, (uint8) 0x00);
		// read the mysterious 0x0001
		success &= expect(inStream, (uint16) 0x0001);
		// read the 'a' byte
		success &= expect(inStream, (uint8) 'a');
		// read the design name length
		readXilinxString(inStream, outDesignName);
		// read the 'b' byte
		success &= expect(inStream, (uint8) 'b');
		// read the device name length
		readXilinxString(inStream, outDeviceName);
		// read the 'c' byte
		success &= expect(inStream, (uint8) 'c');
		// read the design date length
		readXilinxString(inStream, outDesignDate);
		// read the 'd' byte
		success &= expect(inStream, (uint8) 'd');
		// read the design time length
		readXilinxString(inStream, outDesignTime);
		// read the 'e' byte
		success &= expect(inStream, (uint8) 'e');
		// read the inStream length
		inStream.read((char*) &outBitstreamLength, sizeof(outBitstreamLength));
		outBitstreamLength = ntohl(outBitstreamLength);

		// some versions of the tools leave spaces inside the date and time
		size_t pos;
		pos = 0; while((pos = outDesignDate.find(' ', pos)) != string::npos) outDesignDate[pos] = '0';
		pos = 0; while((pos = outDesignTime.find(' ', pos)) != string::npos) outDesignTime[pos] = '0';

		// spit out a debugging string
		cout << "Design " << outDesignName << " (" << outDeviceName << ") @ " << outDesignDate << " " 
			<< outDesignTime << ": " << outBitstreamLength << " bytes (" << (outBitstreamLength >> 2) << " words)" 
			<< endl;

		// return the result
		return success;
	}
	bool bitstream::write(fstream& outStream, uint8 outVal) {
		// read the actual data from the stream
		uint8 actual = outVal;
		outStream.write((char*) &actual, sizeof(actual));
		return true;
	}

	bool bitstream::write(fstream& outStream, uint16 outVal) {
		// read the actual data from the stream
		uint16 actual = htons(outVal);
		outStream.write((char*) &actual, sizeof(actual));
		// return equality
		return true;
	}

	bool bitstream::write(fstream& outStream, uint32 outVal) {
		// read the actual data from the stream
		uint32 actual = htonl(outVal);
		outStream.write((char*) &actual, sizeof(actual));
		// return equality
		return true;
	}

	bool bitstream::write(fstream& outStream, const string& outVal) {
		// look for the length;
		if(!write(outStream, (uint16) outVal.size())) return false;
		// create a buffer to use
		int adjustedLength = outVal.size() + 1;
		char* buffer = new char[adjustedLength];
		// read the actual data from the stream
		outStream.read(buffer, adjustedLength);
		return buffer == outVal;
	}
	void bitstream::writeXilinxString(fstream& outStream, string inString) {
		// write the string length
		uint16 length = inString.size()+1;
		length = htons(length);
		outStream.write((char*) &length, sizeof(length));
		length = ntohs(length) - 1;	
		if(length > 0) {
			// write the null-terminated string
			outStream.write(inString.c_str(), length);
			uint8 null = 0;
			outStream.write((char*) &null, 1);
		}
	}
		

	bool bitstream::writeHeader(fstream& outStream)
	{
		cout << "Writing header" << endl;
		bool success = false;
		// write the magic length
		success &= write(outStream, (uint16) 0x0009);
		// write the magic bytes
		success &= write(outStream, (uint32) 0x0ff00ff0);
		success &= write(outStream, (uint32) 0x0ff00ff0);
		success &= write(outStream, (uint8) 0x00);
		// write the mysterious 0x0001
		success &= write(outStream, (uint16) 0x0001);
		// write the 'a' byte
		success &= write(outStream, (uint8) 'a');
		// write the design name length
		string designName = "partial.ncd;UserID=0xFFFFFFFF";
		writeXilinxString(outStream, designName);
		// write the 'b' byte
		success &= write(outStream, (uint8) 'b');
		// write device string
		string deviceName = my_dev->get_name();
		writeXilinxString(outStream, deviceName.substr(2)+"ff1136");
		// write the 'c' byte
		success &= write(outStream, (uint8) 'c');
		// write design date
		string designDate = "2010/03/18";
		writeXilinxString(outStream, designDate);
		// write the 'd' byte
		success &= write(outStream, (uint8) 'd');
		// write design date
		string designTime = "16:24:32";
		writeXilinxString(outStream, designTime);
		// write the 'e' byte
		success &= write(outStream, (uint8) 'e');
/* We won't write the size here, because we need to wait until we know the size
		uint32 bitstreamSize = 4464512;
		success &= write(outStream, (uint32) bitstreamSize);
*/
		return success;
	}

	bool bitstream::writeBitstream(const string& outBitstream)
	{
			// construct the file stream and open file for writing
			fstream outStream(outBitstream.c_str(), fstream::out);
			if(!outStream.good())
				cerr << "bitstream::writeBitstream: Unable to open bitstream file for writing" << endl;
			// enable fstream exceptions
			outStream.exceptions(fstream::eofbit | fstream::failbit | fstream::badbit);
			bool success = true;
			// Write bitstream header
			success &= writeHeader(outStream);
			// If buildPartial hasn't been called yet, we assume the user wants a full bitstream
			if(frameBitmap.size() == 0)
			{
				frameBitmap.resize(my_dev->get_cfg_size(),true);
				// Write register config and frame packets
				success &= writePackets(outStream);
			}
			else
				success &= writePacketsPartial(outStream);
			outStream.close();
			return success;
	}
	bitstream* bitstream::newBitstream(const string& inFilename) {

		// determine whether our parsing was successful
		bool success = false;
		// fields of interest

		try {

			// construct the file stream and open the file in default mode
			fstream bitstream(inFilename.c_str());
			// enable fstream exceptions
			bitstream.exceptions(fstream::eofbit | fstream::failbit | fstream::badbit);
			success = true;

			// read the header
			string designName, deviceName, designDate, designTime;
			uint32 bitstreamLength;
			success &= readHeader(bitstream, designName, deviceName, designDate, designTime, bitstreamLength);

			// define the known family patterns
			// a fews explanations:
			//		the c or xc at the beginning of the device name are sometimes omitted
			//		the package may follow immediately after the part, but the part may have an e or t suffix
			//		the speed grade may be included (really?)
			boost::regex reVirtex("^x?c?(v[0-9]+)((bg|cs|fg|hq|pq|tq)[0-9]+)?(-[0-9]+)?$", boost::regex_constants::icase);
			boost::regex reVirtexE("^x?c?(v[0-9]+e)((bg|cs|fg|hq|pq|tq)[0-9]+)?(-[0-9]+)?$", boost::regex_constants::icase);
			boost::regex reVirtex2("^x?c?(2v[0-9]+)((bg|cs|ff|fg)[0-9]+)?(-[0-9]+)?$", boost::regex_constants::icase);
			boost::regex reVirtex2P("^x?c?(2vpx?[0-9]+)((ff|fg)[0-9]+)?(-[0-9]+)?$", boost::regex_constants::icase);
			boost::regex reVirtex4("^x?c?(4v[slf]x[0-9]+)((sf|ff)[0-9]+)?(-[0-9]+)?$", boost::regex_constants::icase);
			boost::regex reVirtex5("^x?c?(5v[slf]x[0-9]+t?)((ff)[0-9]+)?(-[0-9]+)?$", boost::regex_constants::icase);
			boost::regex reSpartan3E("^x?c?(3s[0-9]+e)((cp|ft|pq|tq|vq)[0-9]+)?(-[0-9]+)?", boost::regex_constants::icase);
			// return a new bitstream for the target device if we are able to
			//std::cout << std::endl << "comparing " << deviceName << std::endl;
			if(boost::regex_match(deviceName, reVirtex4)) {
				string canonicalDeviceName = boost::regex_replace(deviceName, reVirtex4, "xc$1");
				//std::cout << "canonical name " << canonicalDeviceName << std::endl;
				return new v4_bitstream(canonicalDeviceName);
			} else if(boost::regex_match(deviceName, reVirtex5)) {
				string canonicalDeviceName = boost::regex_replace(deviceName, reVirtex5, "xc$1");
				//std::cout << "canonical name " << canonicalDeviceName << std::endl;
				return new v5_bitstream(canonicalDeviceName);
			}
		}
		
		// catch any file exceptions
		catch(fstream::failure& f) {
			success = false;
			cerr << "Unprocessed fstream::failure: " << f.what() << endl;
			
		}
		catch(std::exception& e) {
			cerr << "Unprocessed exception: " << e.what() << endl;
		}

		return NULL;	
	}

	bool bitstream::loadFile(string bitstream_name) {

		bitstreamFile = bitstream_name;
		// determine whether our parsing was successful
		bool success = false;
		// fields of interest

		try {

			// construct the file stream and open the file in default mode
			fstream bitstream(bitstream_name.c_str());
			// enable fstream exceptions
			bitstream.exceptions(fstream::eofbit | fstream::failbit | fstream::badbit);
			success = true;

			// read the header
			success &= readHeader(bitstream, designName, deviceName, designDate, designTime, bitstreamLength);
			bitstreamWordCount = bitstreamLength >> 2;
			// read the contents
			success &= readPackets(bitstream);
		}
		
		// catch any file exceptions
		catch(fstream::failure& f) {

			success = false;
			cerr << "Unprocessed fstream::failure: " << f.what() << endl;
			
		}
		catch(std::exception& e) {
			cerr << "Unprocessed exception: " << e.what() << endl;
		}

		cout << "Parsing was " << (success ? "successful" : "unsuccessful") << endl;
		return success;	
	}


	uint32 bitstream::calculateFrameECC(uint32 fIndex)
	{
		using boost::dynamic_bitset;
		// The 12th bit is the overall parity bit which allows for 2-bit error detection
		const uint16 parityMask = 1<<11;
		// The numbering of bits for the hamming code starts at 704
		const uint16 startPos = 704;
		// Size of blocks to check at a time (we could probably bump this to 64)
		const uint16 blockSize = 32;
		// Number of ECC bits
		const uint16 eccSize = 12;
		uint16 eccBits = 0;
		// First 320 bits of frame are numbered from 704-1023
		uint16 xorMask=startPos + parityMask;
		int frameWords = my_dev->get_frame_words();
		uint16 skipBits = 0;
		// Loop over all words in the frame to calculate ECC
		for(int i = 0; i < my_dev->get_frame_words(); i++)
		{
			// Initialize bitset with the current frame word
			uint32 *curBlock = (uint32*) frame_array[fIndex] + i;
			dynamic_bitset<> blockBits(blockSize,swapEndian(*curBlock));
			// Next 320 bits are numbered from 1056-1375
			if(i == frameWords/4) xorMask |= blockSize;
			// The ECC calculation skips the ECC bits themselves and the last 660 bits are numbered from 1388-2047
			if(i == frameWords/2) skipBits = eccSize;
			else skipBits = 0;
			if(i == (frameWords/2 + 1)) xorMask += eccSize;
			// Iterate over bits in block and xor with mask if set
			for(int j = skipBits; j < blockSize; j++)
			{
				if(blockBits[j])
					eccBits = eccBits ^ xorMask;
				xorMask++;
			}
		}
		// Calculate parity for ecc word and add for double error detection
		dynamic_bitset<> parityChecker(11,eccBits);
		uint16 parity = parityChecker.count() & 0x1;
		eccBits ^= (parity << 11); 
		if(eccBits != 0)
			cout << "Frame: " << dec << fIndex << " ECC Word: " << hex << eccBits << endl << endl;
		return swapEndian(eccBits);
	}

	uint32 bitstream::swapEndian(const uint32 source) const
	{
		uint32 retVal;
		retVal = source >> 24;
		retVal |= source << 8 & 0x00FF0000; 
		retVal |= source >> 8 & 0x0000FF00; 
		retVal |= source << 24; 
		return retVal;
	}

	bool bitstream::writeCfgMemory(string bitstreamName)
	{
		// Check whether device has been allocated
		if(!my_dev)
			return false;
		// Check whether or not this was a partial bitstream
		if(isPartial)
			return false;
		fstream bitstreamIn(bitstreamFile.c_str(), ios::in | ios::binary);
		fstream bitstreamOut(bitstreamName.c_str(), ios::out | ios::binary);
		// Copy original bitstream
		char buf[1024];
		while(bitstreamIn)
		{
			bitstreamIn.read(buf,1024);
			bitstreamOut.write(buf,bitstreamIn.gcount());
		}
		bitstreamIn.close();
	
		// seek start of configuration memory within bitstream
		bitstreamOut.seekg(cfgMemoryStart);
		// Prepare frame for write and write frame back out
		const size_t bytesPerFrame = my_dev->get_frame_words() << 2;
		for(int i = 0; i < my_dev->get_cfg_size(); i++)
		{
			prepFrameForWrite(i);
			bitstreamOut.write(frame_array[i], bytesPerFrame);
		}
				bitstreamOut.close();
		return true;
	}

	void bitstream::dumpFrames()
	{
		dumpFrames(0,my_dev->get_cfg_size());
	}

	void bitstream::dumpFrames(uint32 startFrame, uint32 endFrame)
	{
		int tileWidth = my_dev->get_tile_width();
		int halfColBytes = (my_dev->get_frame_words() / 2) << 2;
		for(uint32 frame = startFrame; frame < endFrame; frame++)
		{
			frame_addr far = findexToFaddr[frame];
			cout << "frame " << frame << ": " << hex << setfill(' ') << setw(16) << (void*) frame_array[frame] 
				<< " {" << far.type << "," << far.tb << "," << far.row << "," << far.col << "," << far.mna << "} "
				<< dec;
			cout << ": ";
			for(int i = 0; i < my_dev->get_frame_words() << 2; i++) {
				cout << hex << setfill('0') << setw(2) << (uint16) (*(frame_array[frame]+i) & 0xff) << dec;
				if(i < (halfColBytes) && (i % tileWidth) == (tileWidth-1)) cout << ' ';
				if(i > (halfColBytes) && (i % tileWidth) == 3) cout << ' ';
			}
			cout << endl;

		}
	}

	void bitstream::compareBitstreams(const bitstream& bitstreamA, const bitstream& bitstreamB)
	{
		try
		{
			cout << "Comparing two bitstreams" << endl;
			// Make sure we're comparing bitstreams from the same device
			if(bitstreamA.my_dev->get_chip_id() != bitstreamB.my_dev->get_chip_id())
				throw("Cannot compare bitstreams from different parts");
			device *refDev = bitstreamA.my_dev;
			int cfgSize = refDev->get_cfg_size();
			int tileWidth = refDev->get_tile_width();
			int frameWords = refDev->get_frame_words();
			int bytesPerFrame = frameWords << 2;
			char** const frameArrayA = bitstreamA.frame_array;
			char** const frameArrayB = bitstreamB.frame_array;

			for(int i=0;i<cfgSize;i++)
			{
				uint8* byteA = (uint8*) frameArrayA[i];
				uint8* byteB = (uint8*) frameArrayB[i];
				for(int j=0;j<bytesPerFrame;j++)
				{
					if(byteA[j] != byteB[j])
					{
						int tileIndex = refDev->byte_to_tile(j);
						if(tileIndex != -1)
						{
							int byteOffset = j%tileWidth;
							if(j > bytesPerFrame/2)
								byteOffset -= 4;
							frame_addr far = bitstreamA.findexToFaddr[i];
							cout << "A difference was found in: ";
							cout << "tileIndex = " << tileIndex;
							cout << ", frameIndex = " << i << ", far = " << far.str();
							cout << ", and byte_offset = " << dec << byteOffset;
							cout << endl;
							bitstream::compareBytes(byteA[j],byteB[j]);
						}
					}
				}
			}
		}
		catch(string error)
		{
			cerr << "Exception: " << error << endl;
		}
	}
	void bitstream::compareBytes(const uint8 a, const uint8 b) 
	{
		uint8 mask = 0x1;
		bool bitDiff;

		for(int i=0;i<8;i++)
		{
			unsigned short maskVal = (unsigned short) mask;
			if((a & mask) == (b & mask))
				bitDiff = false;
			else
				bitDiff = true;
			if(bitDiff)
			{
				cout << "\t";
				if((a & mask) != 0)
					cout << "Bitmask " << dec << maskVal << " is set in bitstream a\t";
				else
					cout << "Bitmask " << dec << maskVal << " is cleared in bitstream a\t";
				if((b & mask) != 0)
					cout << "Bitmask " << dec << maskVal << " is set in bitstream b\t";
				else
					cout << "Bitmask " << dec << maskVal << " is cleared in bitstream b\t";
				cout << endl;
			}
			mask = mask << 1;
		}
	}
	/**
	 * Build the frameBitmap structure to represent which frames should be written out to
	 * the bitstream when necessary.
	 * @param regionTiles A vector of all the tiles in a partial region.
	 */
	void bitstream::buildPartial(vector<string> regionTiles)
	{
		frameBitmap.resize(my_dev->get_cfg_size(),false);
		for(uint32 i=0;i<regionTiles.size();i++)
		{
			if(tileMap.count(regionTiles[i]))
			{
				tile_data* curTile = tileMap[regionTiles[i]];
				int startFrame = curTile->frame_num;
				int endFrame = startFrame + curTile->num_frames;
				cout << "bitstream::buildPartial: Adding tile " << curTile->name << " to bitmap with far: " << curTile->far.str()
					<< " and goes from frame " << startFrame << " to frame " << endFrame << endl;
				for(int j=startFrame;j<endFrame;j++)
				{
					frameBitmap.set(j);
				}
				// Set frames for this clock region
				frame_addr gclkAddr = my_dev->tilecoord_to_major(my_dev->get_gclk_index(),curTile->coord.y);
				// Increment address since tilecoord_to_major actually returned address for IOB column before GCLK
				gclkAddr.col++; 
				startFrame = my_dev->frame_offset(gclkAddr);
				endFrame = startFrame + my_dev->get_tile_frames(GCLK);
				for(int j=startFrame;j<endFrame;j++)
				{
					frameBitmap.set(j);
				}
			}
		}
	}

	bool bitstream::writeFrames(fstream& outStream, int startFrame, int numFrames)
	{
		try 
		{
			cout << "writeFrames: Writing from " << startFrame << " to " << startFrame+numFrames << endl;
			for(int i=startFrame;i<startFrame+numFrames;i++)
			{
				outStream.write(frame_array[i],my_dev->get_frame_words()<<2);
			}
		}
		catch(std::exception e)
		{
			cerr << "bitstream::writeFrames: Failed writing bitstream" << endl;
			return false;
		}
		return true;
	}
#if 0
	00000000  00 09 0f f0 0f f0 0f f0  0f f0 00 00 01 61 00 0d  |.............a..|
	00000010  72 66 66 74 2e 70 61 72  2e 6e 63 64 00 62 00 0c  |rfft.par.ncd.b..|
	00000020  34 76 6c 78 36 30 66 66  36 36 38 00 63 00 0b 32  |4vlx60ff668.c..2|
	00000030  30 30 39 2f 20 36 2f 20  32 00 64 00 09 31 34 3a  |009/ 6/ 2.d..14:|
	00000040  20 30 3a 35 37 00 65 00  21 cb 30 ff ff ff ff aa  | 0:57.e.!.0.....|
	00000050  99 55 66 20 00 00 00 30  00 80 01 00 00 00 07 20  |.Uf ...0....... |
	00000060  00 00 00 20 00 00 00 30  01 20 01 00 04 3f e5 30  |... ...0. ...?.0|
	00000070  01 80 01 01 6b 40 93 30  00 80 01 00 00 00 09 20  |....k@.0....... |
	00000080  00 00 00 30 00 c0 01 00  00 06 00 30 00 a0 01 00  |...0.......0....|
	00000090  00 06 00 20 00 00 00 20  00 00 00 20 00 00 00 20  |... ... ... ... |
	000000a0  00 00 00 20 00 00 00 20  00 00 00 20 00 00 00 20  |... ... ... ... |
#endif

}
