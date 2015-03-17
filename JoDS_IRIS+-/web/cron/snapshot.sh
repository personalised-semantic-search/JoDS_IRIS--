# Setup
DIR=/var/www/html/iris-reasoner_org/ant/iris
export ANT_HOME=/home/barryb/apache-ant-1.7.0

echo ====================================================================
echo IRIS daily build script
date
echo

echo --------------------------------------------------------------------
echo Change to $DIR directory
cd $DIR

echo --------------------------------------------------------------------
echo Update from source control
#cvs update -d
svn up

echo --------------------------------------------------------------------
echo Copy the latest user guide so that it can be downloaded from the website
cp doc/user_guide/user_guide.pdf ../../pages

echo --------------------------------------------------------------------
echo Build using ant
ant clean
ant release

echo --------------------------------------------------------------------
echo Make all new files group editable
chmod -R g+w *

echo --------------------------------------------------------------------
echo Remove yesterdays snapshot
rm -rf ../../snapshot/*

echo --------------------------------------------------------------------
echo Copy the iris jar files and license
cp build/jar/* ../../snapshot/
cp LICENSE.txt ../../snapshot/
mkdir ../../snapshot/release
cp build/release/*.zip ../../snapshot/release

echo --------------------------------------------------------------------
echo Copy the 3rd party jars and licenses
cp -r lib/ext/jgrapht ../../snapshot

echo --------------------------------------------------------------------
echo Copy the javadoc to the snapshot
cp -r build/javadoc ../../snapshot/

echo --------------------------------------------------------------------
echo Copy the source code zip
cp build/iris-src-*.zip ../../snapshot/

echo --------------------------------------------------------------------
echo Run the junit tests
ant test

echo --------------------------------------------------------------------
echo Again make all the files writable by the group
chmod -R g+w *

echo --------------------------------------------------------------------
echo Remove yesterdays test report
rm -rf ../../snapshot/junit_report

echo --------------------------------------------------------------------
echo Copy the new test report
mv build/report/html/ ../../snapshot/junit_report

echo --------------------------------------------------------------------
echo And try to make sbsolutely sure that everything is group writable
chmod -R g+w ../../snapshot

