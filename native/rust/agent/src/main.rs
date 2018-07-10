extern crate blkid;
extern crate blkid_sys;
extern crate dbus;
extern crate env_logger;
#[macro_use]
extern crate log;
extern crate nix;
extern crate systemd;

mod lib;

use blkid::BlkId;
use dbus::{BusType, Connection, NameFlag};
use dbus::tree::Factory;
use env_logger::Builder;
use log::Log;
use log::Metadata;
use log::Record;
use nix::libc;
use std::fs::File;
use std::io::{BufRead, BufReader};
use std::path::Path;
use systemd::journal;
use std::mem;
use nix::sys::statfs::statfs;
use lib::Partition;

struct CompoundLogger(Vec<Box<Log>>);

impl Log for CompoundLogger {
    fn enabled(&self, metadata: &Metadata) -> bool {
        self.0.iter().any(|l| l.enabled(metadata))
    }

    fn log(&self, record: &Record) {
        self.0.iter().for_each(|l| l.log(record))
    }

    fn flush(&self) {
        self.0.iter().for_each(|l| l.flush())
    }
}

fn main() {
    let env_logger = Builder::from_default_env().build();
    let systemd_logger = journal::JournalLog;
    log::set_max_level(env_logger.filter());
    log::set_boxed_logger(Box::new(CompoundLogger(vec![
        Box::new(env_logger),
        Box::new(systemd_logger),
    ]))).unwrap();
    error!("test");

    let connection = Connection::get_private(BusType::Session).unwrap();
    connection.register_name("com.alesharikwebserver.agent", NameFlag::ReplaceExisting as u32).unwrap();
    let factory = Factory::new_fn::<()>();

    let tree = factory.tree(()).add(factory.object_path("/com/alesharik/webserver/agent", ()).introspectable().add(
        factory.interface("com.alesharik.webserver.agent", ()).add_m(
            factory.method("GetPartitionInfo", (), move |m| {
                let partition: String = m.msg.get1().unwrap();
                let blkid = BlkId::new(Path::new(&partition)).unwrap();
                blkid.do_probe().unwrap();

                let mount_point = get_mount_point(&partition);
                let mount_point2 = mount_point.clone();
                let mount_point3 = mount_point.clone();
                let mut stat: libc::statfs = unsafe { mem::uninitialized() };
                if mount_point.is_some() {
                    statfs(Path::new(&mount_point2.unwrap()), &mut stat).unwrap();
                }


                let part = Partition {
                    address: partition,
                    label: blkid.lookup_value("LABEL").unwrap(),
                    partition_type: blkid.lookup_value("TYPE").unwrap(),
                    mount_point: if mount_point.is_some() { mount_point3.unwrap() as String } else { String::from("none") },
                    max_size: blkid.get_size().unwrap(),
                    sector_count: blkid.get_sectors().unwrap(),
                    sector_size: blkid.get_sectorsize(),
                    free_size: if mount_point.is_some() { stat.f_bfree as i64 * stat.f_frsize } else { -1 },
                    inodes_max: if mount_point.is_some() { stat.f_files as i64 } else { -1 },
                    inodes_free: if mount_point.is_some() { stat.f_ffree as i64 } else { -1 },
                };

                let ret = m.msg.method_return().append1(part);
                Ok(vec!(ret))
            }).outarg::<Vec<String>, _>("reply").inarg::<&str, _>("partition")
        )
    ));
    tree.set_registered(&connection, true).unwrap();
    connection.add_handler(tree);
    loop {
        connection.incoming(1000).next();
    }
}

fn get_mount_point(partition: &String) -> Option<String> {
    let mounts = File::open("/proc/mounts").unwrap();
    let mounts_reader = BufReader::new(mounts);
    for line in mounts_reader.lines() {
        let l = line.unwrap();
        if l.starts_with(partition) {
            let v: Vec<&str> = l.split(' ').collect();
            return Some(String::from(v[1]));
        }
    }
    None
}