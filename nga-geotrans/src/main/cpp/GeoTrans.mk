
#
# This Andoird Makefile was derived from the gnu makefile provided
# in the geotrans7 source tree ~/geotrans3.7/CCS/linux_64/makefile
#
# Any time geotrans source is updates this file must also reflect
# any changes to the build.
#



# line 31
DTCCDIR := $(LOCAL_PATH)/../src/dtcc/CoordinateSystems
CCSERVICEDIR := $(LOCAL_PATH)/../src
SRCDIR := $(LOCAL_PATH)/../../GEOTRANS3/java_gui/geotrans3/jni
JNIDIR := $(LOCAL_PATH)/../../GEOTRANS3/java_gui/geotrans3/jni
#JAVADIR := /usr/jdk1.5.0_18#Note: Change this for your system


# line 51
DTCCSRCS := \
        $(DTCCDIR)/threads/CCSThreadMutex.cpp \
        $(DTCCDIR)/threads/CCSThreadLock.cpp \
        $(DTCCDIR)/albers/AlbersEqualAreaConic.cpp \
        $(DTCCDIR)/azeq/AzimuthalEquidistant.cpp \
        $(DTCCDIR)/bng/BritishNationalGrid.cpp \
        $(DTCCDIR)/bonne/Bonne.cpp \
        $(DTCCDIR)/cassini/Cassini.cpp \
        $(DTCCDIR)/cyleqa/CylindricalEqualArea.cpp \
        $(DTCCDIR)/datum/Datum.cpp \
        $(DTCCDIR)/datum/DatumLibraryImplementation.cpp \
        $(DTCCDIR)/datum/SevenParameterDatum.cpp \
        $(DTCCDIR)/datum/ThreeParameterDatum.cpp \
        $(DTCCDIR)/eckert4/Eckert4.cpp \
        $(DTCCDIR)/eckert6/Eckert6.cpp \
        $(DTCCDIR)/ellipse/Ellipsoid.cpp \
        $(DTCCDIR)/ellipse/EllipsoidLibraryImplementation.cpp \
        $(DTCCDIR)/eqdcyl/EquidistantCylindrical.cpp \
        $(DTCCDIR)/gars/GARS.cpp \
        $(DTCCDIR)/geocent/Geocentric.cpp \
        $(DTCCDIR)/georef/GEOREF.cpp \
        $(DTCCDIR)/gnomonic/Gnomonic.cpp \
        $(DTCCDIR)/grinten/VanDerGrinten.cpp \
        $(DTCCDIR)/lambert/LambertConformalConic.cpp \
        $(DTCCDIR)/loccart/LocalCartesian.cpp \
        $(DTCCDIR)/mercator/Mercator.cpp \
        $(DTCCDIR)/mgrs/MGRS.cpp \
        $(DTCCDIR)/miller/MillerCylindrical.cpp \
        $(DTCCDIR)/misc/CoordinateSystem.cpp \
        $(DTCCDIR)/mollweid/Mollweide.cpp \
        $(DTCCDIR)/neys/Neys.cpp \
        $(DTCCDIR)/nzmg/NZMG.cpp \
        $(DTCCDIR)/omerc/ObliqueMercator.cpp \
        $(DTCCDIR)/orthogr/Orthographic.cpp \
        $(DTCCDIR)/polarst/PolarStereographic.cpp \
        $(DTCCDIR)/polycon/Polyconic.cpp \
        $(DTCCDIR)/sinusoid/Sinusoidal.cpp \
        $(DTCCDIR)/stereogr/Stereographic.cpp \
        $(DTCCDIR)/trcyleqa/TransverseCylindricalEqualArea.cpp \
        $(DTCCDIR)/tranmerc/TransverseMercator.cpp \
        $(DTCCDIR)/ups/UPS.cpp \
        $(DTCCDIR)/usng/USNG.cpp \
        $(DTCCDIR)/utm/UTM.cpp \
        $(DTCCDIR)/webmerc/WebMercator.cpp # JDG missing



CCSSRCS := \
        $(CCSERVICEDIR)/CoordinateConversion/CoordinateConversionService.cpp

CCSERVICESRCS = \
        $(CCSERVICEDIR)/dtcc/DatumLibrary.cpp \
        $(CCSERVICEDIR)/dtcc/EllipsoidLibrary.cpp \
        $(CCSERVICEDIR)/dtcc/GeoidLibrary.cpp \
        $(CCSERVICEDIR)/dtcc/egm2008_geoid_grid.cpp \
        $(CCSERVICEDIR)/dtcc/egm2008_full_grid_package.cpp \
        $(CCSERVICEDIR)/dtcc/egm2008_aoi_grid_package.cpp \
        $(CCSERVICEDIR)/dtcc/CoordinateSystemParameters/CoordinateSystemParameters.cpp \
        $(CCSERVICEDIR)/dtcc/CoordinateSystemParameters/EllipsoidParameters.cpp \
        $(CCSERVICEDIR)/dtcc/CoordinateSystemParameters/EquidistantCylindricalParameters.cpp \
        $(CCSERVICEDIR)/dtcc/CoordinateSystemParameters/GeodeticParameters.cpp \
        $(CCSERVICEDIR)/dtcc/CoordinateSystemParameters/LocalCartesianParameters.cpp \
        $(CCSERVICEDIR)/dtcc/CoordinateSystemParameters/MapProjection3Parameters.cpp \
        $(CCSERVICEDIR)/dtcc/CoordinateSystemParameters/MapProjection4Parameters.cpp \
        $(CCSERVICEDIR)/dtcc/CoordinateSystemParameters/MapProjection5Parameters.cpp \
        $(CCSERVICEDIR)/dtcc/CoordinateSystemParameters/MapProjection6Parameters.cpp \
        $(CCSERVICEDIR)/dtcc/CoordinateSystemParameters/MercatorStandardParallelParameters.cpp \
        $(CCSERVICEDIR)/dtcc/CoordinateSystemParameters/MercatorScaleFactorParameters.cpp \
        $(CCSERVICEDIR)/dtcc/CoordinateSystemParameters/NeysParameters.cpp \
        $(CCSERVICEDIR)/dtcc/CoordinateSystemParameters/ObliqueMercatorParameters.cpp \
        $(CCSERVICEDIR)/dtcc/CoordinateSystemParameters/PolarStereographicStandardParallelParameters.cpp \
        $(CCSERVICEDIR)/dtcc/CoordinateSystemParameters/PolarStereographicScaleFactorParameters.cpp \
        $(CCSERVICEDIR)/dtcc/CoordinateSystemParameters/UTMParameters.cpp \
        $(CCSERVICEDIR)/dtcc/CoordinateTuples/Accuracy.cpp \
        $(CCSERVICEDIR)/dtcc/CoordinateTuples/BNGCoordinates.cpp \
        $(CCSERVICEDIR)/dtcc/CoordinateTuples/CartesianCoordinates.cpp \
        $(CCSERVICEDIR)/dtcc/CoordinateTuples/CoordinateTuple.cpp \
        $(CCSERVICEDIR)/dtcc/CoordinateTuples/GARSCoordinates.cpp \
        $(CCSERVICEDIR)/dtcc/CoordinateTuples/GeodeticCoordinates.cpp \
        $(CCSERVICEDIR)/dtcc/CoordinateTuples/GEOREFCoordinates.cpp \
        $(CCSERVICEDIR)/dtcc/CoordinateTuples/MapProjectionCoordinates.cpp \
        $(CCSERVICEDIR)/dtcc/CoordinateTuples/MGRSorUSNGCoordinates.cpp \
        $(CCSERVICEDIR)/dtcc/CoordinateTuples/UPSCoordinates.cpp \
        $(CCSERVICEDIR)/dtcc/CoordinateTuples/UTMCoordinates.cpp \
        $(CCSERVICEDIR)/dtcc/Exception/ErrorMessages.cpp \
        $(CCSERVICEDIR)/dtcc/Exception/WarningMessages.cpp


SRCS := \
        $(JNIDIR)/strtoval.cpp \
        $(JNIDIR)/fiomeths.cpp

JNISRCS := \
        $(JNIDIR)/JNICCSObjectTranslator.cpp \
        $(JNIDIR)/JNIDatumLibrary.cpp \
        $(JNIDIR)/JNIEllipsoidLibrary.cpp \
        $(JNIDIR)/JNIFiomeths.cpp \
        $(JNIDIR)/ThrowException.cpp \
        $(JNIDIR)/JNICoordinateConversionService.cpp



DTCCINCS := \
        $(DTCCDIR)/threads \
        $(DTCCDIR)/albers \
        $(DTCCDIR)/azeq \
        $(DTCCDIR)/bonne \
        $(DTCCDIR)/bng \
        $(DTCCDIR)/cassini \
        $(DTCCDIR)/cyleqa \
        $(DTCCDIR)/datum \
        $(DTCCDIR)/eckert4 \
        $(DTCCDIR)/eckert6 \
        $(DTCCDIR)/ellipse \
        $(DTCCDIR)/eqdcyl \
        $(DTCCDIR)/gars \
        $(DTCCDIR)/geocent \
        $(DTCCDIR)/geoid \
        $(DTCCDIR)/georef \
        $(DTCCDIR)/gnomonic \
        $(DTCCDIR)/grinten \
        $(DTCCDIR)/lambert \
        $(DTCCDIR)/loccart \
        $(DTCCDIR)/mercator \
        $(DTCCDIR)/misc \
        $(DTCCDIR)/mgrs \
        $(DTCCDIR)/miller \
        $(DTCCDIR)/mollweid \
        $(DTCCDIR)/neys \
        $(DTCCDIR)/nzmg \
        $(DTCCDIR)/omerc \
        $(DTCCDIR)/orthogr \
        $(DTCCDIR)/polarst \
        $(DTCCDIR)/polycon \
        $(DTCCDIR)/sinusoid \
        $(DTCCDIR)/stereogr \
        $(DTCCDIR)/trcyleqa \
        $(DTCCDIR)/tranmerc \
        $(DTCCDIR)/ups \
        $(DTCCDIR)/usng \
        $(DTCCDIR)/utm \
        $(DTCCDIR)/webmerc  # JDG 


INCLUDES := \
        $(CCSERVICEDIR)/dtcc \
        $(CCSERVICEDIR)/CoordinateConversion \
        $(CCSERVICEDIR)/dtcc/CoordinateSystemParameters \
        $(CCSERVICEDIR)/dtcc/CoordinateTuples \
        $(CCSERVICEDIR)/dtcc/Enumerations \
        $(CCSERVICEDIR)/dtcc/Exception \
        $(SRCDIR)

JNIINCS := \
        $(JNIDIR)

#JAVAINCS = \
#        $(JAVADIR)/include \
#        $(JAVADIR)/include/linux



# line 15:
# posic = -shared -fPIC -m64 -pthread -std=gnu++98 -Wno-deprecated  -D_GNU_SOURCE -DREDHAT
GEO_TRANS_CFLAGS := -pthread -std=gnu++98 -Wno-deprecated -D_GNU_SOURCE -DREDHAT

