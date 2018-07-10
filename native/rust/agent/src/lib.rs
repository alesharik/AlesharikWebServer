extern crate dbus;

use dbus::arg::{Append, Get, Iter, IterAppend};

pub struct Partition {
    pub address: String,
    pub label: String,
    pub partition_type: String,
    pub mount_point: String,
    pub max_size: i64,
    pub sector_count: i64,
    pub sector_size: u32,
    pub free_size: i64,
    pub inodes_max: i64,
    pub inodes_free: i64,
}

impl Append for Partition {
    fn append(self, iter: &mut IterAppend) {
        iter.append(self.address);
        iter.append(self.label);
        iter.append(self.partition_type);
        iter.append(self.mount_point);
        iter.append(self.max_size);
        iter.append(self.sector_count);
        iter.append(self.sector_size);
        iter.append(self.free_size);
        iter.append(self.inodes_max);
        iter.append(self.inodes_free);
    }
}

impl<'a> Get<'a> for Partition {
    fn get(i: &mut Iter<'a>) -> Option<Partition> {
        let s = Partition {
            address: i.get()?,
            label: i.get()?,
            partition_type: i.get()?,
            mount_point: i.get()?,
            max_size: i.get()?,
            sector_count: i.get()?,
            sector_size: i.get()?,
            free_size: i.get()?,
            inodes_max: i.get()?,
            inodes_free: i.get()?,
        };
        Some(s)
    }
}
