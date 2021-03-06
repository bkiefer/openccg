// Tests might fail if you have creative characters in your path.  Sue me.  
#include "util/file_piece.hh"

#include "util/file.hh"
#include "util/scoped.hh"

#define BOOST_TEST_MODULE FilePieceTest
#include <boost/test/unit_test.hpp>
#include <fstream>
#include <iostream>

#include <stdio.h>
#include <sys/types.h>
#include <sys/stat.h>

namespace util {
namespace {

std::string FileLocation() {
  if (boost::unit_test::framework::master_test_suite().argc < 2) {
    return "file_piece.cc";
  }
  std::string ret(boost::unit_test::framework::master_test_suite().argv[1]);
  return ret;
}

/* istream */
BOOST_AUTO_TEST_CASE(IStream) {
  std::fstream ref(FileLocation().c_str(), std::ios::in);
  std::fstream backing(FileLocation().c_str(), std::ios::in);
  FilePiece test(backing);
  std::string ref_line;
  while (getline(ref, ref_line)) {
    StringPiece test_line(test.ReadLine());
    BOOST_CHECK_EQUAL(ref_line, test_line);
  }
  BOOST_CHECK_THROW(test.get(), EndOfFileException);
  BOOST_CHECK_THROW(test.get(), EndOfFileException);
}

/* mmap implementation */
BOOST_AUTO_TEST_CASE(MMapReadLine) {
  std::fstream ref(FileLocation().c_str(), std::ios::in);
  FilePiece test(FileLocation().c_str(), NULL, 1);
  std::string ref_line;
  while (getline(ref, ref_line)) {
    StringPiece test_line(test.ReadLine());
    // I submitted a bug report to ICU: http://bugs.icu-project.org/trac/ticket/7924
    if (!test_line.empty() || !ref_line.empty()) {
      BOOST_CHECK_EQUAL(ref_line, test_line);
    }
  }
  BOOST_CHECK_THROW(test.get(), EndOfFileException);
}

#if !defined(_WIN32) && !defined(_WIN64) && !defined(__APPLE__)
/* Apple isn't happy with the popen, fileno, dup.  And I don't want to
 * reimplement popen.  This is an issue with the test.  
 */
/* read() implementation */
BOOST_AUTO_TEST_CASE(StreamReadLine) {
  std::fstream ref(FileLocation().c_str(), std::ios::in);

  std::string popen_args = "cat \"";
  popen_args += FileLocation();
  popen_args += '"';

  FILE *catter = popen(popen_args.c_str(), "r");
  BOOST_REQUIRE(catter);
  
  FilePiece test(dup(fileno(catter)), "file_piece.cc", NULL, 1);
  std::string ref_line;
  while (getline(ref, ref_line)) {
    StringPiece test_line(test.ReadLine());
    // I submitted a bug report to ICU: http://bugs.icu-project.org/trac/ticket/7924
    if (!test_line.empty() || !ref_line.empty()) {
      BOOST_CHECK_EQUAL(ref_line, test_line);
    }
  }
  BOOST_CHECK_THROW(test.get(), EndOfFileException);
  BOOST_REQUIRE(!pclose(catter));
}
#endif

#ifdef HAVE_ZLIB

// gzip file
BOOST_AUTO_TEST_CASE(PlainZipReadLine) {
  std::string location(FileLocation());
  std::fstream ref(location.c_str(), std::ios::in);

  std::string command("gzip <\"");
  command += location + "\" >\"" + location + "\".gz";

  BOOST_REQUIRE_EQUAL(0, system(command.c_str()));
  FilePiece test((location + ".gz").c_str(), NULL, 1);
  unlink((location + ".gz").c_str());
  std::string ref_line;
  while (getline(ref, ref_line)) {
    StringPiece test_line(test.ReadLine());
    // I submitted a bug report to ICU: http://bugs.icu-project.org/trac/ticket/7924
    if (!test_line.empty() || !ref_line.empty()) {
      BOOST_CHECK_EQUAL(ref_line, test_line);
    }
  }
  BOOST_CHECK_THROW(test.get(), EndOfFileException);
}

// gzip stream.  Apple doesn't like popen, fileno, dup.  This is an issue with
// the test.  
#ifndef __APPLE__
BOOST_AUTO_TEST_CASE(StreamZipReadLine) {
  std::fstream ref(FileLocation().c_str(), std::ios::in);

  std::string command("gzip <\"");
  command += FileLocation() + "\"";

  FILE * catter = popen(command.c_str(), "r");
  BOOST_REQUIRE(catter);
  
  FilePiece test(dup(fileno(catter)), "file_piece.cc.gz", NULL, 1);
  std::string ref_line;
  while (getline(ref, ref_line)) {
    StringPiece test_line(test.ReadLine());
    // I submitted a bug report to ICU: http://bugs.icu-project.org/trac/ticket/7924
    if (!test_line.empty() || !ref_line.empty()) {
      BOOST_CHECK_EQUAL(ref_line, test_line);
    }
  }
  BOOST_CHECK_THROW(test.get(), EndOfFileException);
  BOOST_REQUIRE(!pclose(catter));
}
#endif // __APPLE__

#endif // HAVE_ZLIB

} // namespace
} // namespace util
