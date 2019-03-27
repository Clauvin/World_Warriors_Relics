package ww_relics.relics.chun_li;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.evacipated.cardcrawl.modthespire.lib.SpireConfig;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.AbstractCard.CardType;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.cards.CardGroup.CardGroupType;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.PowerTip;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.rooms.AbstractRoom;

import basemod.abstracts.CustomRelic;

public class SpikyBracers extends CustomRelic {
	
	public static final String ID = "WW_Relics:Spiky_Bracers";
	
	private static final int UPDATE_COST_BY = -1;
	private static final int UPDATE_COST_TEXT = -UPDATE_COST_BY;
	private static final int NUMBER_OF_CARDS_TO_APPLY_EFFECT = 2;
	
	public AbstractCard[] cards_chosen;
	public static int NUMBER_OF_CARDS_CHOSEN = 0;
	
	public static boolean cards_are_selected = false;
	public boolean power_tip_updated = false;
	
	static Logger logger = LogManager.getLogger(SpikyBracers.class.getName());
	
	public SpikyBracers() {
		super(ID, "abacus.png", //add method for textures here.
				RelicTier.COMMON, LandingSound.HEAVY);
		NUMBER_OF_CARDS_CHOSEN = 0;
		cards_chosen = new AbstractCard[NUMBER_OF_CARDS_TO_APPLY_EFFECT];
	}
	
	public String getUpdatedDescription() {
		
		String base_description = DESCRIPTIONS[0] + NUMBER_OF_CARDS_TO_APPLY_EFFECT+
				DESCRIPTIONS[1] + UPDATE_COST_TEXT +
				DESCRIPTIONS[2];

		/*base_description += DESCRIPTIONS[3];
		for (int i = 0; i < cards_chosen.length; i++) {
			if (cards_chosen[i] == null) {
				base_description += DESCRIPTIONS[5];
			} else {
				base_description += FontHelper.colorString(cards_chosen[i].name, "y");
				base_description += DESCRIPTIONS[4];
			}

		}

		base_description += FontHelper.colorString(cards_chosen[1].name, "y");
		base_description += DESCRIPTIONS[5];*/
		
		return base_description;
	}
	
	public String getUncoloredDescription() {
		return DESCRIPTIONS[6] + NUMBER_OF_CARDS_TO_APPLY_EFFECT+
				DESCRIPTIONS[7] + UPDATE_COST_TEXT +
				DESCRIPTIONS[8];
	}
	
	public void updateTipPostCardsChosen() {
		String text_for_tip = getUpdatedDescription();
		this.tips.clear();
		this.tips.add(new PowerTip(this.name, text_for_tip));
		initializeTips();
	}
		
	public CardGroup getValidCardGroup() {
		
		CardGroup valid_card_group = new CardGroup(CardGroupType.UNSPECIFIED);
		
		CardGroup powers = AbstractDungeon.player.masterDeck.getPowers();
		CardGroup skills = AbstractDungeon.player.masterDeck.getSkills();
		
		for (int i = 0; i < powers.size(); i++) {
			if (powers.getNCardFromTop(i).cost >= 2) {
				valid_card_group.addToTop(powers.getNCardFromTop(i));
			}
		}
		
		for (int i = 0; i < skills.size(); i++) {
			if (skills.getNCardFromTop(i).cost >= 2) {
				valid_card_group.addToTop(skills.getNCardFromTop(i));
			}
		}
		
		return valid_card_group;

	}
	
	@SuppressWarnings("static-access")
	public void update()
	{
		super.update();

	    if (cards_are_selected && !power_tip_updated) {
			updateTipPostCardsChosen();
			power_tip_updated = true;
		}
	}
	
	public void onUnequip() {
		resetRelic();
	}
	
	public void onEnterRoom(AbstractRoom room) {
		resetRelic();
	}
	
	public void resetRelic() {
		if (cards_are_selected) {		
			cards_chosen = new AbstractCard[NUMBER_OF_CARDS_TO_APPLY_EFFECT];
			cards_are_selected = false;
			power_tip_updated = false;
			NUMBER_OF_CARDS_CHOSEN = 0;
		}
	}
	
	public void onUseCard(AbstractCard card, UseCardAction action) {
		/*logger.info("NUMBER_OF_CARDS_CHOSEN " + NUMBER_OF_CARDS_CHOSEN);
		logger.info("cards_chosen.length " + cards_chosen.length);
		logger.info("NUMBER_OF_CARDS_TO_APPLY_EFFECT " + NUMBER_OF_CARDS_TO_APPLY_EFFECT);*/
		
		if ((NUMBER_OF_CARDS_CHOSEN < cards_chosen.length) && (NUMBER_OF_CARDS_CHOSEN < NUMBER_OF_CARDS_TO_APPLY_EFFECT)) {
			if (cardCanReceiveEffect(card)){
				cards_chosen[NUMBER_OF_CARDS_CHOSEN] = card.makeCopy();
				card.modifyCostForCombat(UPDATE_COST_BY);
				NUMBER_OF_CARDS_CHOSEN++;
				cards_are_selected = true;
			}
		}
	}
	
	public boolean cardCanReceiveEffect(AbstractCard card) {
		boolean power_or_skill = (card.type == CardType.POWER) || (card.type == CardType.SKILL);
		boolean cost_equal_or_higher_than_2 = card.cost >= 2;
		
		boolean has_been_chosen_already = cardHasBeenChosenAlready(card);
		
		return power_or_skill && cost_equal_or_higher_than_2 && !has_been_chosen_already;
	}
	
	public boolean cardHasBeenChosenAlready(AbstractCard card) {
		boolean has_been_chosen_already = false;
		
		for (AbstractCard card_chosen: cards_chosen) {
			if (card_chosen != null) {
				if (card_chosen.uuid == card.uuid) {
					has_been_chosen_already = true;
					break;
				}
			}
		}
		
		return has_been_chosen_already;
	}
	
	public boolean canSpawn()
	{
		CardGroup powers;
		int number_of_powers_costing_2_or_more = 0;

		CardGroup skills;
		int number_of_skills_costing_2_or_more = 0;
		
		powers = AbstractDungeon.player.masterDeck.getPowers();
		number_of_powers_costing_2_or_more = countNumberOfValidCards(powers);
		
		skills = AbstractDungeon.player.masterDeck.getSkills();
		number_of_skills_costing_2_or_more = countNumberOfValidCards(skills);
		
		return (number_of_powers_costing_2_or_more + number_of_skills_costing_2_or_more)
					>= NUMBER_OF_CARDS_TO_APPLY_EFFECT; 
	}
	
	public int countNumberOfValidCards(CardGroup card_group) {
		int number_of_cards_costing_2_or_more = 0;
		
		card_group.sortByCost(false);
		
		for (int i = 0; i < card_group.size(); i++) {
			if (card_group.getNCardFromTop(i).cost >= 2) number_of_cards_costing_2_or_more += 1;
		}
		
		return number_of_cards_costing_2_or_more;
	}
	
	public static void save(final SpireConfig config) {


        if (AbstractDungeon.player != null && AbstractDungeon.player.hasRelic(ID)) {
    		logger.info("Started saving SpikyBracers information");
        	final SpikyBracers relic = (SpikyBracers)AbstractDungeon.player.getRelic(ID);

        	if (relic.cards_chosen != null) {
                config.setInt("spiky_bracers_1",
                		AbstractDungeon.player.masterDeck.group.indexOf(relic.cards_chosen[0]));
                config.setInt("spiky_bracers_2",
                		AbstractDungeon.player.masterDeck.group.indexOf(relic.cards_chosen[1]));
        	}

            config.setBool("spiky_cards_are_selected", cards_are_selected);
            
            try {
				config.save();
			} catch (IOException e) {
				e.printStackTrace();
			}
            logger.info("Finished saving Spike Bracers info.");
        }
        else {
        	clear(config);
        }

    }
	
	public static void load(final SpireConfig config) {
		
		logger.info("Loading Spiky Bracers info.");
		if (AbstractDungeon.player.hasRelic(ID) && config.has("spiky_bracers_1") &&
				config.getBool("spiky_cards_are_selected")) {

            final SpikyBracers relic = (SpikyBracers)AbstractDungeon.player.getRelic(ID);
            final int cardIndex_1 = config.getInt("spiky_bracers_1");
            final int cardIndex_2 = config.getInt("spiky_bracers_2");
            
            logger.info(cardIndex_1 + " " + cardIndex_2);
            
        	relic.cards_chosen = new AbstractCard[NUMBER_OF_CARDS_TO_APPLY_EFFECT];
            
            if (cardIndex_1 >= 0 &&
            		cardIndex_1 < AbstractDungeon.player.masterDeck.group.size()) {
            	logger.info("Tried to load here 3.");
            	loadSpikyCard(relic, cardIndex_1, 0);
            }
            
            if (cardIndex_2 >= 0 &&
            		cardIndex_2 < AbstractDungeon.player.masterDeck.group.size()) {
            	logger.info("Tried to load here 4.");
            	loadSpikyCard(relic, cardIndex_2, 1);
            }
            
            cards_are_selected = true;      
            
            try {
				config.load();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            logger.info("Finished loading Spiky Bracers info.");
        }
		
		else
		{
			logger.info("There's no info, setting variables accordingly.");
			cards_are_selected = false;
			logger.info("Finished setting Spiky Bracers variables.");
		}
		
		
    }
	
	public static void loadSpikyCard(SpikyBracers relic, int index, int position) {
		
    	relic.cards_chosen[position] = AbstractDungeon.player.masterDeck.group.get(index);
    	
        if (relic.cards_chosen[position]!= null) {
        	relic.cards_chosen[position].updateCost(UPDATE_COST_BY);
        }
	}
	
	public static void clear(final SpireConfig config) {
		logger.info("Clearing Spiky Bracers variables.");      
        config.remove("spiky_bracers_1");
        config.remove("spiky_bracers_2");
        config.remove("spiky_cards_are_selected");
        logger.info("Finished clearing Spiky Bracers variables.");
	}
	
	public AbstractRelic makeCopy() { // always override this method to return a new instance of your relic
		return new SpikyBracers();
	}

}
