package me.hupeng.java.monitorserver.Mina;


import me.hupeng.java.monitorserver.util.OperateImage;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/**
 * Created by HUPENG on 2016/9/4.
 */
public class MyImageDecoder extends CumulativeProtocolDecoder {

    @Override
    protected boolean doDecode(IoSession ioSession, IoBuffer ioBuffer, ProtocolDecoderOutput protocolDecoderOutput) throws Exception {
        if(ioBuffer.remaining() > 6){//前4字节是包头
            //标记当前position的快照标记mark，以便后继的reset操作能恢复position位置
            ioBuffer.mark();
//            byte[] l = new byte[4];
//            ioBuffer.get(l);
            //包体数据长度
            
            int len = ioBuffer.getInt();
            int client_id = ioBuffer.getInt();
            System.out.println("原来的文件长度：" +  len);
            System.out.println("包里面文件总长度：" + ioBuffer.remaining());
            //int len = MyTools.bytes2int(l);//将byte转成int



            if (len == 0){
                ioBuffer.position(ioBuffer.position()+ioBuffer.limit()-4);
                return true;
            }

            //注意上面的get操作会导致下面的remaining()值发生变化
            if(ioBuffer.remaining() < len){
                //如果消息内容不够，则重置恢复position位置到操作前,进入下一轮, 接收新数据，以拼凑成完整数据
                ioBuffer.reset();
                return false;
            }else{
                //消息内容足够

                ioBuffer.reset();
                int length = ioBuffer.getInt();
                client_id = ioBuffer.getInt();
                System.out.println("接受文件长度：" +  length);

                byte dest[] = new byte[length];
                ioBuffer.get(dest);
                
                //图片裁剪
                dest = OperateImage.cut(dest);
                MyData myData = new MyData();
                myData.bitmap = dest;
                myData.clientId = client_id;
                protocolDecoderOutput.write(myData);
                
                return true;
            }
        }
        return false;//处理成功，让父类进行接收下个包
    }

    

}
