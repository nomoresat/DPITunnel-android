# Set the minimum version of CMake that can be used
cmake_minimum_required(VERSION 3.5)

# Set the project name
project(DPITunnel-cli)

include(ExternalProject)

set(OPENSSL_VERSION "1.1.1m")

# Add OpenSSL as external project
ExternalProject_Add(OpenSSL
        URL https://www.openssl.org/source/openssl-${OPENSSL_VERSION}.tar.gz
        CONFIGURE_COMMAND ${CMAKE_COMMAND} -E env PATH=${CMAKE_C_COMPILER_EXTERNAL_TOOLCHAIN}/bin:$ENV{PATH} CC=${CMAKE_C_COMPILER} ANDROID_NDK_HOME=${ANDROID_NDK} perl <SOURCE_DIR>/Configure android-${ANDROID_ARCH_NAME} no-shared -D__ANDROID_API__=${ANDROID_NATIVE_API_LEVEL} --openssldir=${CMAKE_LIBRARY_OUTPUT_DIRECTORY} --libdir="" --prefix=${CMAKE_LIBRARY_OUTPUT_DIRECTORY}
        UPDATE_COMMAND ""
        BUILD_COMMAND ${CMAKE_COMMAND} -E env PATH=${CMAKE_C_COMPILER_EXTERNAL_TOOLCHAIN}/bin:$ENV{PATH} ANDROID_NDK_HOME=${ANDROID_NDK} make
        BUILD_BYPRODUCTS ${CMAKE_LIBRARY_OUTPUT_DIRECTORY}/libssl.a ${CMAKE_LIBRARY_OUTPUT_DIRECTORY}/libcrypto.a
        BUILD_IN_SOURCE 1
        )

file(MAKE_DIRECTORY ${CMAKE_LIBRARY_OUTPUT_DIRECTORY}/include)
set(OPENSSL_INCLUDE_DIR ${CMAKE_LIBRARY_OUTPUT_DIRECTORY}/include)
set(OPENSSL_SSL_LIBRARY ${CMAKE_LIBRARY_OUTPUT_DIRECTORY}/libssl.a)
set(OPENSSL_CRYPTO_LIBRARY ${CMAKE_LIBRARY_OUTPUT_DIRECTORY}/libcrypto.a)

add_library(OpenSSL::SSL STATIC IMPORTED)
add_dependencies(OpenSSL::SSL OpenSSL)
set_target_properties(OpenSSL::SSL PROPERTIES IMPORTED_LOCATION ${CMAKE_LIBRARY_OUTPUT_DIRECTORY}/libssl.a)
set_target_properties(OpenSSL::SSL PROPERTIES INTERFACE_INCLUDE_DIRECTORIES ${CMAKE_LIBRARY_OUTPUT_DIRECTORY}/include)

add_library(OpenSSL::Crypto STATIC IMPORTED)
add_dependencies(OpenSSL::Crypto OpenSSL)
set_target_properties(OpenSSL::Crypto PROPERTIES IMPORTED_LOCATION ${CMAKE_LIBRARY_OUTPUT_DIRECTORY}/libcrypto.a)
set_target_properties(OpenSSL::Crypto PROPERTIES INTERFACE_INCLUDE_DIRECTORIES ${CMAKE_LIBRARY_OUTPUT_DIRECTORY}/include)

set(STATIC_BINARY true)
add_subdirectory(dpitunnel-cli)

target_include_directories(DPITunnel-cli-exec
        PRIVATE
        dpitunnel-cli-android-includes
)

# Rename binary to lib<name>.so to add it in result APK
set_target_properties(
        DPITunnel-cli-exec
        PROPERTIES
        OUTPUT_NAME "libdpitunnel-cli"
        SUFFIX ".so"
)