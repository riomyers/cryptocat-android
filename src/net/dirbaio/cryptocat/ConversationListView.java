package net.dirbaio.cryptocat;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import net.dirbaio.cryptocat.service.CryptocatMessage;

import java.util.ArrayList;

public class ConversationListView extends ListView
{
    public ConversationListView(Context context) {
        super(context);
    }

    public ConversationListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ConversationListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    ConversationAdapter conversationArrayAdapter;

    public void setHistory(ArrayList<CryptocatMessage> history)
    {
        conversationArrayAdapter = new ConversationAdapter(getContext(), history);
        setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        setAdapter(conversationArrayAdapter);
    }

    public void notifyDataSetChanged()
    {
        conversationArrayAdapter.notifyDataSetChanged();
    }

    Paint paint = new Paint();

    //Trick to make the code below simpler.
    public static final CryptocatMessage dummyMessage = new CryptocatMessage(CryptocatMessage.Type.Error, "dummy", "dummy");

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        paint.setColor(Color.RED);
        paint.setStrokeWidth(0);

        final int count = getChildCount();

        boolean inGroup = false;
        String groupNick = "";
        int left = 0, top = 0, right = 0, bottom = 0;

        for(int i = 0; i <= count; i++)
        {
            CryptocatMessage msg = dummyMessage;
            View v = null;
            ViewGroup vg = null;

            if(i != count)
            {
                //Every message_* layout is a LinearLayout to align something to the left/center/right
                //We're interested in the position of its child, so we get it this way.
                vg = (ViewGroup) getChildAt(i);
                ViewHolder holder = (ViewHolder)vg.getTag();
                if(holder == null)
                    continue;

                msg = holder.message;
                v = vg.getChildAt(0);
            }

            //See if one group ends
            if(inGroup)
            {
                if((msg.type != CryptocatMessage.Type.Message && msg.type != CryptocatMessage.Type.MessageMine) || !msg.nickname.equals(groupNick))
                {
                    //Group has been broken
//                    canvas.drawRect(vg.getLeft()+v.getLeft(), vg.getTop()+v.getTop(), vg.getLeft()+v.getRight(), vg.getTop()+v.getBottom(), paint);
                    canvas.drawRect(left, top, right, bottom, paint);
                    inGroup = false;
                }
                else
                {
                    //group continues
                    left = min(left, vg.getLeft()+v.getLeft());
                    top = min(top, vg.getTop()+v.getTop());
                    right = max(right, vg.getLeft()+v.getRight());
                    bottom = max(bottom, vg.getTop()+v.getBottom());
                }
            }

            //See if new group starts
            if(!inGroup && (msg.type == CryptocatMessage.Type.Message || msg.type == CryptocatMessage.Type.MessageMine))
            {
                inGroup = true;
                groupNick = msg.nickname;

                left = vg.getLeft()+v.getLeft();
                top = vg.getTop()+v.getTop();
                right = vg.getLeft()+v.getRight();
                bottom = vg.getTop()+v.getBottom();
            }
        }
    }

    private static int min(int a, int b)
    {
        return a<b?a:b;
    }
    private static int max(int a, int b)
    {
        return a>b?a:b;
    }

    private static class ViewHolder {
        TextView nickname;
        TextView text;
        CryptocatMessage message;
    }

    private class ConversationAdapter extends ArrayAdapter<CryptocatMessage>
    {

        private Context context;

        public ConversationAdapter(Context context, ArrayList<CryptocatMessage> items)
        {
            super(context, 0, items);
            this.context = context;
        }

        @Override
        public int getViewTypeCount() {
            return CryptocatMessage.Type.values().length;
        }

        @Override
        public int getItemViewType(int position) {
            return getItem(position).type.ordinal();
        }

        public View getView(int position, View view, ViewGroup parent)
        {
            CryptocatMessage msg = getItem(position);
            ViewHolder holder;

            if (view == null)
            {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                int id;

                switch (msg.type)
                {
                    case Message:       id = R.layout.message; break;
                    case MessageMine:   id = R.layout.message_mine; break;
                    case Join:          id = R.layout.message_join; break;
                    case Leave:         id = R.layout.message_leave; break;
                    case File:          id = R.layout.message_error; break;
                    case Error:         id = R.layout.message_error; break;
                    default: throw new IllegalStateException("Unknown item type");
                }

                view = inflater.inflate(id, null);

                holder = new ViewHolder();

                if(msg.type != CryptocatMessage.Type.Error && msg.type != CryptocatMessage.Type.MessageMine)
                    holder.nickname = (TextView) view.findViewById(R.id.nickname);
                if(msg.type != CryptocatMessage.Type.Join && msg.type != CryptocatMessage.Type.Leave)
                    holder.text = (TextView) view.findViewById(R.id.text);

                view.setTag(holder);
            }
            else
                holder = (ViewHolder) view.getTag();

            holder.message = msg;

            boolean showNick = true;

            if(msg.type == CryptocatMessage.Type.Message && position != 0)
            {
                CryptocatMessage prevMsg = getItem(position-1);
                if(prevMsg.type == CryptocatMessage.Type.Message && prevMsg.nickname.equals(msg.nickname))
                    showNick = false;
            }

            if(msg.type != CryptocatMessage.Type.Error && msg.type != CryptocatMessage.Type.MessageMine)
            {
                holder.nickname.setVisibility(showNick ? View.VISIBLE : View.GONE);
                holder.nickname.setText(msg.nickname);
            }

            if(msg.type != CryptocatMessage.Type.Join && msg.type != CryptocatMessage.Type.Leave)
            {
                holder.text.setText(msg.text);
            }


            return view;
        }
    }

}
