extern crate alehsarik_web_server_agent;
extern crate dbus;
extern crate jni;
extern crate nix;
extern crate regex;

use alehsarik_web_server_agent::Partition;
use dbus::{BusType, Connection, Message};
use jni::{JavaVM, JNIEnv};
use jni::errors::Error;
use jni::objects::{GlobalRef, JClass, JObject, JValue};
use jni::sys::{jint, jlong, jlongArray, JNI_VERSION_1_8, jobject, jsize};
use nix::sys::sysinfo::*;
use regex::Regex;
use std::fs::{DirEntry, File, read_dir};
use std::io::{BufRead, BufReader};
use std::option::Option;

static mut MEMORY_UTILS_CLASS: Option<GlobalRef> = None;
static mut SYSTEM_INFO_UTILS_CLASS: Option<GlobalRef> = None;
static mut CORE_UTILS_CLASS: Option<GlobalRef> = None;
static mut FILE_SYSTEM_UTILS_CLASS: Option<GlobalRef> = None;


fn proc_update(env: &JNIEnv) -> Result<(), Error> {
    let stat = File::open("/proc/stat").unwrap();
    let reader = BufReader::new(stat);
    let reg = Regex::new("cpu[0-9]*.*").unwrap();
    let return_array: Vec<jlongArray> = reader.lines().into_iter()
        .filter_map(|l| l.ok())
        .filter_map(|l| reg.find(&l)
            .map(|m| m.as_str().to_string())
            .map(|s| s.as_str()[(s.find(' ').unwrap() + 1)..].to_string()))
        .map(|s| s.split(" ")
            .map(|s| String::from(s))
            .collect::<Vec<String>>())
        .map(|v: Vec<String>| v.into_iter()
            .filter_map(|s| s.parse::<i64>().ok())
            .map(|i| i as jlong)
            .collect())
        .map(|a: Vec<jlong>| {
            let arr = env.new_long_array(a.len() as i32).expect("Can't create long array!");
//            let r = env.new_global_ref(JObject::from(arr)).expect("Can't create global ref on long array!");
            env.set_long_array_region(arr, 0, &a).expect("Can't copy Vec<jlong> into array!");
            arr
        })
        .collect();
    let ret = env.new_object_array(return_array.len() as i32, "[J", JObject::null()).expect("Can't create [[J array!");
    let ret_ref = env.new_global_ref(JObject::from(ret)).expect("Can't create global ref on [[J array!");
    for i in 0..return_array.len() {
        let arr: jlongArray = return_array[i];
        env.set_object_array_element(ret, i as jsize, JObject::from(arr))?;
    }
    unsafe {
        env.call_static_method(JClass::from(CORE_UTILS_CLASS.as_ref().unwrap().as_obj()), "update", "([[J)V", &vec![JValue::from(ret_ref.as_obj())])?;
    }
    Ok(())
}

fn fs_update(env: &JNIEnv) -> Result<(), Error> {
    unsafe {
        env.call_static_method(JClass::from(FILE_SYSTEM_UTILS_CLASS.as_ref().unwrap().as_obj()), "startUpdate", "()V", &vec![])?;
    }
    let connection = Connection::get_private(BusType::Session).unwrap();
    for dev in read_dir("/sys/block").unwrap() {
        let entry: DirEntry = dev.unwrap();
        let path = entry.path().into_os_string();
        let device_name = entry.file_name().into_string().unwrap();
        let nm: jobject = env.new_string(path.into_string().unwrap())?.into_inner();

        unsafe {
            env.call_static_method(JClass::from(FILE_SYSTEM_UTILS_CLASS.as_ref().unwrap().as_obj()), "startUpdateDevice", "(Ljava/lang/String;)V", &vec![JValue::from(JObject::from(nm))])?;
        }

        let parts: Vec<String> = read_dir(entry.path()).unwrap()
            .filter_map(|e| e.ok())
            .filter(|e| e.file_name().into_string().unwrap().starts_with(&device_name.as_str()))
            .map(|e| e.path().into_os_string().into_string().unwrap())
            .collect();

        for part in parts {
            let partition_address = part.clone();
            let p_result = get_partition_info(env, &connection, &partition_address);
            if p_result.is_err() {
                return Ok(());
            }
            let p = p_result.unwrap();
            let args = vec![JValue::from(JObject::from(env.new_string(&partition_address).unwrap())),
                            JValue::from(JObject::from(env.new_string(p.label).unwrap())),
                            JValue::from(JObject::from(env.new_string(p.partition_type).unwrap())),
                            JValue::from(JObject::from(env.new_string(p.mount_point).unwrap())),
                            JValue::from(p.max_size as jlong),
                            JValue::from(p.free_size as jlong),
                            JValue::from(p.inodes_max as jlong),
                            JValue::from(p.inodes_free as jlong),
                            JValue::from(p.sector_count as jlong),
                            JValue::from(p.sector_size as jint)];

            unsafe {
                env.call_static_method(JClass::from(FILE_SYSTEM_UTILS_CLASS.as_ref().unwrap().as_obj()), "updatePartition", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;JJJJJI)V", &args)?;
            }
        }

        unsafe {
            env.call_static_method(JClass::from(FILE_SYSTEM_UTILS_CLASS.as_ref().unwrap().as_obj()), "endUpdateDevice", "()V", &vec![])?;
        }
    }
    unsafe {
        env.call_static_method(JClass::from(FILE_SYSTEM_UTILS_CLASS.as_ref().unwrap().as_obj()), "endUpdate", "()V", &vec![])?;
    }
    Ok(())
}

fn get_partition_info(env: &JNIEnv, connection: &Connection, partition_address: &String) -> Result<Partition, ()> {
    let res = get_partition_info_unsafe(connection, partition_address);
    if res.is_ok() {
        Ok(res.unwrap())
    } else {
        env.throw_new("java/lang/Error", format!("DBus error: {}", res.err().unwrap())).unwrap();
        Err(())
    }
}

fn get_partition_info_unsafe(connection: &Connection, partition_address: &String) -> Result<Partition, dbus::Error> {
    let msg = Message::new_method_call("com.alesharikwebserver.agent", "/com/alesharik/webserver/agent", "com.alesharik.webserver.agent", "GetPartitionInfo").unwrap()
        .append1(&partition_address);
    let r = connection.send_with_reply_and_block(msg, 2000)?;
    Ok(r.get1().unwrap())
}

fn memory_update(env: &JNIEnv) -> Result<(), Error> {
    let info = sysinfo().unwrap();
    let mut ret: [JValue; 6] = [JValue::from(-1); 6];
    ret[0] = JValue::from(info.ram_total() as jlong);
    ret[1] = JValue::from(info.ram_unused() as jlong);
//    ret[2] = JValue::from(info.0.sharedram as jlong);
//    ret[3] = JValue::from(info.0.bufferram as jlong);
    ret[4] = JValue::from(info.swap_total() as jlong);
    ret[5] = JValue::from(info.swap_free() as jlong);
    unsafe {
        env.call_static_method(JClass::from(MEMORY_UTILS_CLASS.as_ref().unwrap().as_obj()), "update", "(JJJJJJ)V", &ret)?;
    }
    Ok(())
}

fn info_update(env: &JNIEnv) -> Result<(), Error> {
    let info = sysinfo().unwrap();
    let mut ret: [JValue; 1] = [JValue::from(-1); 1];
    let uptime = info.uptime();
    ret[0] = JValue::from((uptime.as_secs() * 1000 + (uptime.subsec_nanos() / 1000 / 1000) as u64) as i64);
    unsafe {
        env.call_static_method(JClass::from(SYSTEM_INFO_UTILS_CLASS.as_ref().unwrap().as_obj()), "update", "(J)V", &ret)?;
    }
    Ok(())
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_alesharik_webserver_platform_MemoryUtils_update(env: JNIEnv, _class: JClass) {
    let res = memory_update(&env);
    if res.is_err() {
        if env.exception_check().unwrap() {
            let e = env.exception_occurred().unwrap();
            env.throw(e).unwrap();
        } else {
            println!("{}", res.err().unwrap());
        }
    }
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_alesharik_webserver_platform_FileSystemUtils_update(env: JNIEnv, _class: JClass) {
    let res = fs_update(&env);
    if res.is_err() {
        if env.exception_check().unwrap() {
            let e = env.exception_occurred().unwrap();
            env.throw(e).unwrap();
        } else {
            println!("{}", res.err().unwrap());
        }
    }
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_alesharik_webserver_platform_CoreUtils_update(env: JNIEnv, _class: JClass) {
    let res = proc_update(&env);
    if res.is_err() {
        if env.exception_check().unwrap() {
            let e = env.exception_occurred().unwrap();
            env.throw(e).unwrap();
        } else {
            println!("{}", res.err().unwrap());
        }
    }
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_alesharik_webserver_platform_SystemInfoUtils_update(env: JNIEnv, _class: JClass) {
    let res = info_update(&env);
    if res.is_err() {
        if env.exception_check().unwrap() {
            let e = env.exception_occurred().unwrap();
            env.throw(e).unwrap();
        } else {
            println!("{}", res.err().unwrap());
        }
    }
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn JNI_OnLoad(vm: JavaVM, _reserved: &mut ()) -> jint {
    let env = vm.get_env().unwrap();
    unsafe {
        MEMORY_UTILS_CLASS = Some(env.new_global_ref(JObject::from(env.find_class("com/alesharik/webserver/platform/MemoryUtils").unwrap())).unwrap());
        SYSTEM_INFO_UTILS_CLASS = Some(env.new_global_ref(JObject::from(env.find_class("com/alesharik/webserver/platform/SystemInfoUtils").unwrap())).unwrap());
        CORE_UTILS_CLASS = Some(env.new_global_ref(JObject::from(env.find_class("com/alesharik/webserver/platform/CoreUtils").unwrap())).unwrap());
        FILE_SYSTEM_UTILS_CLASS = Some(env.new_global_ref(JObject::from(env.find_class("com/alesharik/webserver/platform/FileSystemUtils").unwrap())).unwrap());
    }
    return JNI_VERSION_1_8;
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn JNI_OnUnload(_vm: JavaVM, _reserved: &mut ()) {
    unsafe {
        MEMORY_UTILS_CLASS = None;
        SYSTEM_INFO_UTILS_CLASS = None;
        CORE_UTILS_CLASS = None;
        FILE_SYSTEM_UTILS_CLASS = None;
    }
}